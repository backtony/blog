## Nginx 기본 구성
```sh
worker_processes  1;

events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        access_log  logs/host.access.log  combined;

        location / {
            root   html;
            index  index.html index.htm;
        }

  
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

    }


}
```
nginx.conf의 기본 파일 경로는 `/usr/local/nginx/conf/nginx.conf`에 존재합니다. 위 코드는 기본 설정 파일에서 주석들을 걸러내고 몇 가지 수정한 코드입니다. 하나씩 살펴봅시다.


## HTTP 핵심 모듈

> 핵심 모듈 : https://runebook.dev/ko/docs/nginx/http/ngx_http_core_module?
> 모듈 변수 : http://nginx.org/en/docs/http/ngx_http_core_module.html#variables 

http 핵심 모듈ㅇ레는 여러 가지 모듈 지시어가 있고 기본적으로 제공되는 많은 변수들을 가지고 있어 지시어의 값으로도 사용ㅇ할 수 있습니다. 변수가 허용되지 않는 지시어의 값에 변수를 사용하면 아무런 오류 없이 변수 이름을 그대로 문자로 사용하므로 주의가 필요합니다.


### 지시어 값의 축약
+ k 또는 K : 킬로바이트
+ m 또는 M : 메가바이트
+ g 또는 G : 기가바이트
+ ms : 밀리초
+ s : 초(기본적인 시간 단위)
+ m : 분
+ h : 시간
+ d : 일
+ w : 주
+ M : 달
+ y : 년

```sh
# 둘다 동일한 의미
client_max_body size 2G;
client_max_body size 2048M;

# 동일한 의미
client_body_timeout 180;
client_body_timeout 3m;
```
```sh
# 결합해서 사용 가능
client_body_timeout 1m30s;

# 값을 띄어쓰기로 구분하는 경우 따옴표 필요
client_body_timeout '1m 30s 500ms';
```

<br>

### 변수
+ 모듈은 지시어 값을 정의할 때 사용할 수 있는 변수를 제공합니다.
+ 변수는 항상 `$`로 시작한다.
+ log_format 지시어를 설정할 때 형식 문자열에 모든 종류의 변수를 포함시킬 수 있습니다.

```sh
location ^~ /admin/ {
    access_log logs/main.log;
    log_format main '$pid - $nginx_version -$remote_addr';
}
```
<br>

하지만 일부 지시어는 변수를 사용할 수 없습니다. 아래 error_log에서는 변수가 치환되지 않고 그대로 문자가 들어갑니다.
```sh
error_log logs/error-$nginx_version.log;
```

<br>

### 문자열 값
지시어 값으로 사용할 수 있는 문자열은 세 가지 형식이 있습니다.
+ 따옴표 없이 입력
  + root /home/example.com/www;
+ 공백이나 세미콜론(;) 또는 중괄호({}) 같은 특수문자를 사용하고 싶을때는 앞에 역슬래시를 문자 앞에 붙이거나, 전체 문자열을 작은 따옴표나 큰따옴표로 묶어야 합니다.
  + root '/home/example.com/my web pages';
+ 엔진엑스에서는 작은따옴표와 큰따옴표는 아무런 차이가 없습니다.
  + `$`앞에 역슬래시를 붙이지만 않는다면 문자열 안에서 삽입된 변수는 정상적으로 확장됩니다.


### worker
```sh
worker_processes  1;

events {
    worker_connections  1024;
}
```
+ worker_processes 1;
  - 작업자 프로세스 하나만 시작하게 하는데, 모든 요청이 하나의 실행 경로로 처리되며, CPU 코어 하나로 실행됨을 의미합니다.
  - 이 값은 CPU 코어당 최소 하나의 프로세스를 갖도록 설정하는 것이 권장됩니다.
  - 값을 auto로 주는 경우 엔진엑스가 최적의 값을 결정합니다.
+ worker_connections 1024;
  - 작업자 프로세스의 수와 함께 서버가 동시에 수용할 수 있는 연결 수를 결정합니다.
  - 예를 들어 각각 1024개의 연결을 수용하는 작업자 프로세스가 4개라면 서버는 동시 연결을 최대 4096개까지 처리하게 됩니다.
  - 이 설정은 보유한 하드웨어에 맞게 조정해야 합니다.

**cf) 엔진엑스 프로세스 아키텍처**
> 엔진엑스를 시작하면 유일한 프로세스인 주 프로세스가 생기는데, 현재 사용자와 그룹의 권한으로 실행됩니다. 시스템이 부딩될 때 init 스크립트로 엔진엑스 서비스가 실행되면 보통 root 사용자와 root 그룹 권한을 가집니다. 주 프로세스는 클라이언트의 요청을 스스로 처리하지는 않고 대신 그 일을 처리해줄 작업자 프로세스를 만듭니다. 작업자 프로세스는 별도로 정의한 사용자와 그룹으로 실행될 수 있으며 작업자 프로세스의 수, 작업자 프로세스당 최대 연결 수, 작업자 프로세스를 실행하는 사용자와 그룹 등을 구성파일로 정의할 수 있습니다. 

### 구조 블록
```sh
http {
    include       mime.types;
    gzip  on;

    server {
        listen       80;
        server_name  localhost;

        location /downloads/ {
            gzip off;
        }
    }
}
```
+ include
  + include 지시어는 이름 그대로 지정된 파일을 포함시킵니다.
  + 별도의 구성을 재사용하기 위해 파일로 분리하고 가져올 때 사용됩니다.
  + mime.types 과 같이 사전에 nginx에서 정의된 것들을 가져올 수도 있습니다.
  + 와일트 카드를 사용하여 한번에 include 시킬 수 있습니다. 단일 파일 include의 경우 파일이 없으면 검증에 실패하지만 와일드 카드의 경우 파일이 있든 없든 통과합니다.
+ http
  - 이 블록은 구성 파일의 최상위에 삽입됩니다.
  - http와 관련된 모듈 지시어와 블록은 http 블록에만 정의할 수 있습니다.
+ server
  - 웹 사이트 하나를 선언할 수 있는 블록입니다.
  - 엔진엑스가 특정 웹사이트(하나 이상의 호스트 이름, 예를 들어, www.website.com 같은 이름으로 식별됨)을 인식하고 그 구성을 얻는 블록입니다.
  - http 블록 안에서만 사용할 수 있습니다.
+ location
  - 요청 경로에 따른 처리를 정의합니다.
  - 웹 사이트의 특정 위치에만 적용되는 설정을 정의하는 데 쓰는 블록입니다.
  - 이 블록은 server 블록 안이나 다른 location 블록 안에 중첩해서 사용할 수 있습니다.

### listen
```sh
listen [주소][:포트] [추가옵션];
# ex    
listen 127.0.0.1:8080;
listen 127.0.0.1;
listen 80 default_server;
listen 443 ssl;
listen 443 ssl http2;
```

여기에 나열된 지시어로 가상 호스트를 구성할 수 있습니다. 가상 호스트는 호스트 이름이나 ip주소와 포트의 조합으로 식별되는 server 블록을 만들어 실현됩니다. 웹 사이트를 제공하는 소켓을 여는 데 사용되는 ip 주소나 포트, 또는 두 가지 모두를 지정할 수 있습니다. 그리고 아래와 같은 추가 옵션을 지정할 수 있습니다.

- default_server : 해당 server 블록을 지정된 ip 주소와 포트로 들어온 모든 요청의 기본 웹 사이트로 지정
- ssl : 웹 사이트가 SSL을 통해 제공되도록 지정
- http2 : http_v2 모듈이 있을 경우 HTTP/2 프로토콜을 지원하도록 활성화


### server_name
```sh
server_name 호스트이름1 [호스트이름2...];
# ex
server_name www.website1.com;
server_name *www.website1*.com;
server_name www.website1.com www.website2.com;
seveer_name ~^(www)\.example\.com$
```

server 블록에 하나 이상의 호스트 이름을 할당하는 지시어입니다. 엔진엑스는 HTTP 요청을 받을 때 요청의 Host 헤더를 server 블록 모두와 비교합니다. 그 중 호스트 이름과 맞는 첫 번째 server 블록이 선택됩니다. 만약 여러 블록이 같은 listen 지시어를 가지고 있다면 server_name은 요청이 전달될 서버 블록을 찾는 역할을 합니다. 매칭되는 server_name이 없다면 listen의 default_server에 해당하는 server 블록으로 매칭됩니다. 이 블록은 정규식과 와일드카드를 사용할 수 있습니다. 정규식을 사용할 때 호스트 이름은 `~`문자로 시작해야 합니다. 지시어 값에 빈 문자열을 사용해 host 헤더 없이 들어오는 모든 요청을 받게 할 수도 있습니다. 다만 적어도 하나의 정규 호스트 이름(또는 더미 호스트 이름인 "_" 문자)이 앞에 있어야 합니다.

```sh
server_name website.com "";
sever_name _ "";
```


<br>

### MIME 타입
```sh
http {
    include       mime.types;
}
```
엔진엑스는 MIME 타입을 구성하는 데 유용한 두 가지 지시어 types와 default_type을 제공합니다. 이 둘은 문서의 기본 MIME 타입을 정의하며, 응답에 포함돼 보내질 Content-Type HTTP 헤더에 영향을 줍니다. types는 MIME 타입과 파일 확장자의 상관관계를 맺는데 쓰이며 어떤 파일을 제공할 때 파일 확장자를 확인해서 MIME 타입을 결정합니다.

### 로그
```sh
http {
    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

  server {
        access_log  logs/host.access.log  combined;
  }    
}

```
로그 파일을 쓸 경로를 지정할 수 있습니다. nginx에서 로그 format으로 제공하는 combined format을 사용할 수도 있고 정의해서 사용할 수도 있습니다. 

### 경로와 문서
각 웹사이트가 제공할 문서를 구성하는 지시어로, 최상위 문서 위치, 사이트 색인, 오류 페이지 같은 것입니다.
```sh
http {
    server {
        server_name localhost;
        root /home/website.com/html;
        location /admin/ {
            alias /var/www/locked/;
        }
    }
}    
```

+ root
  - 방문자에게 제공하고자 하는 파일을 담고 있는 최상위 문서 위치를 정의
+ alias
  - location 블록 안에서만 사용
  - 특정 요청에서 별도의 경로 문서를 읽도록 할당


```sh
server {
    error_page code1 [code2...] [=대체코드] [@block | URI]
    # ex
    error_page 404 /not_found.html;
    error_page 400 @notfound; # 지정한 location 블록으로 이동
    error_page 404 =200 /index.html # 404오류인 경유 200으로 바꾸고 index.html로 경로를 돌림
}
```
error_page 지시어는 http 응답 코드에 맞춰 URI를 조작하거나 이 코드를 다른 코드로 대체합니다.

<br>

### 클라이언트 요청

```sh
http {
    client_body_buffer_size     16k;  
    client_header_buffer_size   8k;
    large_client_header_buffers 4 8k; 
    client_max_body_size        1m;   
    client_header_timeout       5s;  
    client_body_timeout         5s;  
    send_timeout                5s;   
    keepalive_timeout           10s;
}
```
클라이언트 요청에 대한 제한을 정의할 수도 있습니다.

+ client_body_buffer_size
  - POST 등의 요청에 포함되는 body에 대한 버퍼 크기 (default 16k)
  - 요청의 크기가 너무 크면 본문이나 그 일부가 디스크에 저장
  - client_body_in_file_only 지시어가 활성화되면 요청 본문의 크기에 관계없이 언제나 디스크의 파일로 저장됨
+ client_header_buffer_size
  - 요청 헤더에 대한 버퍼 크기 (default 1k)로 토큰 등과 같은 값을 고려
+ large_client_header_buffers
  - 클라이언트가 보낸 헤더가 옵션의 크기나 갯수를 초과할 때 400이 발생
  - URI가 버퍼 하나의 크기보다 크다면 414 발생
+ client_max_body_size
  - 리퀘스트 body 사이즈에 대한 최대 크기
+ client_header_timeout
  - 클라이언트 요청 후 응답의 헤더를 보내기까지 기다리는 시간
+ client_body_timeout
  - 클라이언트 요청 후 응답의 바디 보내기까지 기다리는 시간
  - 기간 초과 시 408 오류를 반환
+ send_timeout
  - 성공한 요청 사이 대기 시간
  - 지정된 시간이 지난 후에 엔진엑스가 비활성 상태의 연결을 닫는다.
  - 클라이언트가 데이터 전송을 중단하는 순간부터 연결은 비활성 상태가 된다.

<br>

### gzip
```sh
gzip            on;
gzip_comp_level 9;
gzip_min_length 1000;
gzip_proxied    expired no-cache no-store private auth;
gzip_types      text/plain application/json application/javascript application/x-javascript text/xml text/css application/xml;
```
클라이언트에 전송하기 전에 gzip 알고리즘으로 응답의 본문을 압축할 수 있게 해줍니다.
+ gzip_comp_level
  - 압축 정도
  - 1(압축률은 낮지만 빠름, 기본값) ~ 9(압축이 많이 되지만 느리다)
+ gzip_min_length
  - 응답 본문의 길이가 지정된 값보다 작으면 압축하지 않습니다.
  - 기본값 0
+ gzip_proxied
  - 프록시에서 받은 응답의 본문을 Gzip으로 압축할지 여부를 결정
  - off/any : 모든 요청을 압축할지 여부 결정
  - expired : expires 헤더가 캐싱을 하지 않게 돼 있다면 압축을 활성화
  - no-cache/no-store/private : cache-control 헤더가 no-cache나 nostore, private으로 설정됐으면 압축을 활성화
  - no_last_modified : last-modified 헤더가 없는 경우에 압축을 활성화
  - no_etag : ETag 헤더가 없을 경우 압축을 활성화
  - auth: Authorization 헤더가 있으면 압축을 활성화
+ gzip_types
  - 기본 MIME 타입인 text/html 외의 다른 타입에 압축을 활성화

### charset
```sh
http {
  charset utf-8;  
}
```
응답 content-type 헤더에 특정 인코딩을 추가합니다.

### map

```sh
http {
    map $uri $variable {
      /page.html 0;
      /contact.html 1;
      default 0;
  }
  rewrite % /index.php?page=$variable;
}

```
한 변수의 값에 따라 그에 대응하는 값을 새 변수에 할당합니다. uri 값이 page.html이면 variable 변수의 값은 0이 되는 형식입니다. `~(대소문자 구분)` 또는 `~*(대소문자 구분 안함)`으로 시작하는 패턴 형태로 정규식도 사용할 수 있습니다.
```sh
map $http_referer $ref {
    ~google "Google";
    ~* yahho "yahho";
    \~bing "bing"; # 앞에 \이 있어 정규식이 아님
    default $http_referer; # 변수 사용
}
```


### disable_symlinks
심볼릭 링크를 웹으로 제공해야 할 때 다루는 방법을 제어합니다. default는 off로 심볼릭 링크가 허용되며, nginx는 링크가 가리키는 파일을 찾습니다. 다음 값 중 하나를 사용해서 특정 조건에 심볼릭 링크가 가리키는 파일을 따라가며 찾지 않도록 할 수 있습니다. 

- on : 요청 URI의 특정 부분이 심볼릭 링크라면 이 접근은 거부되고 403을 반환
- if_not_owner : on 과 비슷하지만 링크와 링크가 가리키는 대상의 소유자가 서로 다를 때 접근 거부
  

<br>

### location 블록
```sh
server {
    location /admin/ {

    }
}

location [=|~|~*|@] 패턴 { ... }
```
폴터 이름 대신 패턴을 사용해서 처리할 수 있습니다. 패턴 앞쪽에 생략 가능한 인자는 위치 조정 부호라고 부릅니다. 

**조정 부호 생략**

```sh
location /abcd
```

- URI가 지정된 패턴으로 시작해야 한다.
- 정규식은 사용할 수 없다.
- 운영체제가 대소문자를 구분하는 파일 시스템을 사용할 때에만 대소문자를 구분한다.
- querystring은 상관 없다.

**=**
```sh
location = /abcd
```
- 지정된 패턴과 정확히 일치해야 한다.
- 정규식은 사용할 수 없고 단순한 문자열이여야 한다.
- 운영체제가 대소문자를 구분하는 파일 시스템을 사용할 때에만 대소문자를 구분한다.
- querystring은 상관 없다.

**~**
```sh
# /로 시작하고 d로 끝난다.
location ~ ^/abcd$
```
- 지정된 정규식에 일치하는지 비교하면서 대소문자를 구분한다.
- querystring은 상관 없다.

__~*__
```sh
location ~* ^/abcd$
```
정규식에 일치하는지 비교하면서 대소문자를 구분하지 않습니다.

<br>

+ ^~
  - 조정 부호가 생략된 경우와 비슷하게 동작한다.
  - 지정된 패턴으로 시작해야 한다.
  - 패턴이 일치하면 다른 패턴을 찾지 않는 다는 것이 차이점이다.
+ @
  - 이름이 지정된 location 블록을 정의한다.
  - 외부 클라이언트는 이 블록에 직접 접근할 수 없고 try_files나 error_page 같은 다른 지시어에 의해 생성된 내부 요청만 가능하다.

<br>

#### location 탐색 순서와 우선 순위
location 블록의 순서와 관계 없이 특정 순서로 일치하는 패턴을 탐색한다.
```sh
location /doc{}
location ~* ^/document$ {}
```
`http://website.com/document` 로 요청을 보낼 때 `~*`가 조정 부호가 우선순위가 높아서 두 번째 블록이 적용됩니다.

<Br>

```sh
location /document {}
location ~* ^/document$ {}
```
`http://website.com/document`로 요청을 보내면 첫번째 블록을 적용합니다. 결과적으로 엔진엑스는 정규식보다 구체적인 문자열을 우선시 한다.  
<br>

```sh
location ^~ /doc {}
location ~* ^/document$ {}
```
`http://website.com/document`로 요청을 보내면 첫번째 블록을 적용합니다. `^~`가 `~*`보다 우선됩니다.

<br>

### 기타
+ resolver
  - 엔진엑스가 호스트 이름으로 ip 주소를 찾거나 그 반대 작업을 할 때 사용할 DNS 서버를 지정
+ resolver_timeout
  - 호스트 이름 ip 변환 요청의 제한시간
+ server_tokens
  - 엔진엑스가 실행되는 버전 정보를 클라이언트에게 알릴지 여부를 정의
    - on : 정보를 제공(기본)
    - off : 서버 헤더에는 엔진엑스를 쓴다는 사실만 기록
    - build : 컴파일 시 --build 스위치에 지정한 값이 노출
+ underscores_in_headers
  - 사용자 정의 http 헤더 이름에 밑줄 부호를 허용할지 여부
  - 기본은 off
+ proxy_headers_hash_bucket_size
  - 프록시 헤더의 해시 테이블용 버킷 크기를 설정한다.
+ proxy_set header
  - 뒷단 서버로 전송될 헤더 값을 다시 정의할 수 있게 한다.
+ proxy_http_version
  - 프록시 뒷단과 통신하는 데 쓰일 HTTP 버전을 설정한다.
  - 기본값은 1.0인데 연결 유지해서 재사용하려면 1.1로 설정해야 한다.
+ proxy_cookie_domain, proxy_cookie_path
  - 쿠키의 도메인이나 경로 속성을 실시작으로 조작하게 한다.
  - 대소문자를 따지지 않는다.

## 재작성 모듈

### 내부 요청
엔진엑스에서는 외부 요청과 내부 요청을 구분합니다. 외부 요청은 클라이언트에서 직접 온 요청을 의미하고 이는 location 블록에 매칭됩니다.
```sh
server {
    location = /document.html {
        deny all;
    }
}
```

`http://website.com/document.html`로 들어오는 클라이언트 지시어는 이 location 블록에 정의대로 직접 처리되지만 이와 반대로 내부 요청은 엔진엑스에서 특수한 지시어에 의해 발생합니다. 기본 엔진엑스 모듈에서 제공되는 지시어에는 내부 요청을 발생시키는 지시어가 여럿인데, error_page, index, rewrite, try_files 외에 첨가 모듈의 add_before_body, add_after_body, ssl 명령 등이 있습니다. 내부 요청은 2가지 유형이 있습니다.

+ 내부 경로 재설정
  - 엔진엑스는 클라이언트 요청을 내부에서 경로를 변경해 처리합니다.
  - 원래 URI가 바뀌기 때문에 이 요청은 다른 location 블록에 매칭되어 다른 설정이 적용됩니다.
  - 내부 요청이 쓰이는 가장 일반적인 경우는 rewrite 지시어가 사용되는 경우로 이 지시어는 요청 URI를 재작성합니다.
+ 부가 요청
  - 원래 요청을 보완할 내용을 생성하고자 내부에서 추가로 새로 만들어지는 요청이 있습니다.
  - 단순한 예로 첨가 모듈이 있고 add_after_body 지시어는 원래 URI 후에 특정 URI가 처리되게 할 수 있습니다.

<Br>

### error_page 지시어
error_page는 특정 오류 코드가 발생했을 때 서버의 행위를 정의합니다.
```sh
server {
    error_page 403 /errors/forbidden.html;
}
```
클라이언트가 오류 중 하나를 일으키는 URI에 접근하려고 할 때 엔진엑스는 오류 코드에 연관된 페이지를 제공합니다. 사실 클라이언트에게 오류 페이지를 전송할 뿐 아니라 URI에 따라 새로운 요청을 발생시킵니다.

```sh
server {
    error_page 404 /errors/404.html;
    location /erros/ {
        internal;
    }
}
```
클라이언트가 존재하지 않는 문서를 읽으려 할 때 404 오류를 수신합니다. 위에서는 error_page 지시어를 사용해 404 오류가 발생하면 내부적으로 /errors/404.html로 경로가 재설정되도록 지정되어있습니다. 따라서 엔진엑스는 /errors/404.html로 새로운 요청을 생성합니다. 이 URI는 location 블록의 /errors/에 대응하게 되고 해당 구성이 적용됩니다. location 블록 내부에 `internal`지시어로 인해 해당 블록은 클라이언트가 /errors/ 디렉터리에 접근하지 못하도록 차단되고 내부 요청만 허용합니다.

### 재작성
error_page 지시어가 다른 위치로 경로를 재설정하는 방식과 비슷하게 rewrite 지시어로 URI를 재작성하면 내부 경로 재설정이 발생합니다.
```sh
server {
    location /storage/ {
        internal;
    }
    location /documents/ {
        rewrite ^/documents/(.*)$ /storage/$1;
    }
}
```
`http://website.com/documents/file.txt`로 들어오는 클라이언트 요청은 처음에는 두번째 location 블록에서 처리되지만 rewrite를 통해 storage로 변환되면서 요청 처리과정을 처음부터 다시 시작하여 location 첫 번째 블록으로 들어가게 됩니다. 그리고 해당 location은 internal이 명시되어있으므로 내부 통신만 허용됩니다.

### 조건부 구조
```sh
server {
    if ($request_method = POST) {

    }
}
```
if 조건부 구조로 어떤 구성을 특정 조건에서만 적용하게 할 수 있습니다. if 지시어를 통해 location을 대체할 수도 있는데 location을 사용하는 이유는 location 블록에는 대부분의 지시어를 사용할 수 있기 때문입니다. 보통 if 블록 안에는 재작성 모듈의 지시어만 넣는 것이 권장됩니다.


## 접근 모듈
allow와 deny가 접근 모듈을 통해 제공됩니다. 이 지시어는 특정 ip 주소나 ip 주소 범위에서 자원에 접근하도록 허가하거나 거절할 수 있습니다.
```sh
location {
    allow 127.0.0.1; # 로컬 ip 주소를 허용
    allow unix:; # 유닉스 도메인 소켓을 허용
    deny all; # 모든 ip 주소 차단
}
```
`규칙들이 위에서 아래러 처리된다는 점을 주의해야 한다.`  
첫 명령이 deny all이면 그 뒤에 따르는 모든 allow 예외 조건이 아무런 효력을 발휘하지 못합니다. 또한, allow all로 시작하면 그 후에 적힌 모든 deny 지시어는 무효가 됩니다.

<Br>

## auth_request 모듈
부가 요청의 결과에 따라 자원 접근을 허락할지 거부할지 결정합니다. 엔진엑스는 auth_request 지시어에 지정한 URI를 호출해서 이런 부가 요청이 2XX 응답 코드를 반환하면 접근을 허용합니다. 부가 요청이 401이나 403 상태 코드를 반환하면 접근이 거부되고 엔진엑스는 해당 응답 코드를 클라이언트에게 전달합니다.
```sh
location /downloads/ {
    # 스크립트가 200 상태를 반환하면 자료를 다운로드를 허용한다.
    auth_request /something/request;
}
```
이 모듈을 auth_request_set 지시어를 통해 부가 요청이 수행된 후에 변수 값을 설정할 수 있게 합니다. 부가 요청에서 기인하는 `$upstream_http_server`나 다른 서버 응답의 HTTP 헤더 값을 `$upstream_http_*` 형태의 변수로 삽입할 수 있습니다.
```sh
location /downloads/ {
    auth_reqeust /authorization.php;

    # 인증이 허용됐다고 가정하고, 부가 요청 응답 헤더에서 파일명을 취해 경로를 재설정한다.
    auth_request_set $filename "${upstream_http_x_filename}.zip";
    rewrite ^ /documents/$filename;
}
```

## 현황 모듈
활성 연결 횟수, 처리된 총 요청 횟수 등 서버의 현재 상태에 대한 정보를 제공합니다. 모듈을 활성화하려면 location 블록에 stub_status 지시어를 추가합니다.
```sh
lcocation = /nginx_status {
    stub_status on;
    allow 127.0.0.1; # 정보를 외부에 노출하지 않음
    deny all;
}
```
이 location 블록에 해당하는 요청은 상태 페이지를 얻게 됩니다.

<Br>

## 엔진엑스 프록시 모듈
```sh
proxy_pass http://호스트명:포트
proxy_pass http://$server_name:8080;

upstream backend {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
}
location ~* \.php$ {
    proxy_pass http://backend;
}
```
+ proxy_pass
  - 위치를 알려서 요청이 뒷단 서버로 전달되도록 지정합니다.
  - upstream을 사용할 수도 있다.
  - 변수를 사용할 수도 있다.
+ proxy_hide_header
  - 기본적으로 엔진엑스는 뒷단 서버에서 받아 클라이언트에게 돌려줄 응답을 준비하기 때문에 Date, Server, X-pad, X-Accel-* 같은 몇 가지 헤더를 무시합니다.
  - 이 지시어로 클라이언트로 전달하지 않고 숨길 헤더를 추가로 지정할 수 있다.
+ proxy_redirect
  - 뒷단 서버에서 유발된 경로를 재설정으로 Location HTTP 헤더 속 URL을 재작성한다.
  - off : 경로 재설정 그대로 전달한다.
  - default : proxy_pass 지시어의 값을 호스트 이름으로 사용하고 현재 경로의 문서를 추가한다.
  - 구성 파일이 순차적으로 해석되기 때문에 proxy_redirect 지시어는 proxy_pass 지시어 다음에 들어가야 한다.
  - URL : URL의 일부를 다른 값으로 대체한다.

<Br>

## 캐시, 버퍼링, 임시 파일
이상적으로는 가능한 뒷단 서버로 전달되는 요청의 수를 줄여야 합니다. 다음 지시어는 캐시 시스템을 구축할 때는 물론 버퍼링 제어 옵션과 엔진엑스가 임시 파일을 다루는 방법을 제어하는데 도움됩니다.

+ proxy_buffering
  - 뒷단 서버에서 오는 응답을 버퍼에 담을지 여부를 정의한다.
  - on으로 설정 시 엔진엑스는 버퍼가 제공하는 메모리 공간을 사용해서 응답 데이터를 메모리에 저장한다.
  - 버퍼가 가득차면 응답 데이터는 임시 파일로 저장될 것이다.
  - off면 응답은 그대로 클라이언트에게 전달된다.
+ proxy_max_temp_file_size
  - 0으로 설정 시 프록시 전달에 적합한 요청에 임시 파일을 사용하지 않게 된다.
  - 임시 파일을 쓰고 싶다면 최대 파일 크기를 설정한다.

<Br>

## 한계치, 시간 제약, 오류
+ proxy_read_timeout
  - 뒷단 서버에서 데이터를 읽는 제한시간을 정한다.
  - 이 시간 제약은 전체 응답 지연이 아닌 읽는 두 작업 사이에 적용된다.
  - 기본 60s
+ proxy_ignore_client_abort
  - on으로 설정하면 클라이언트가 요청을 취소했더라도 엔진엑스는 프록시 요청을 계속 처리한다.
  - off인 경우 엔진엑스도 뒷단 서버로 보내는 요청을 취소한다.

