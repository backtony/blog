
## 출처(Origin)이란?
![그림1](./1.png)

출처(Origin)은 Scheme(protocol), Host, Port로 구성되어 있습니다. http, https의 경우에는 프로토콜이 포트 번호를 포함하고 있기 때문에 생략 가능하지만, 만약 포트를 명시함다면 포트번호까지 일치해야 출처가 같다고 볼 수 있습니다. 즉, scheme, host, port가 모두 일치하는 경우를 동일한 출처로 봅니다.  

```sh
# 기준이 되는 origin
https://www.backtony.github.io 

# 같은 origin
https://www.backtony.github.io/about 
https://www.backtony.github.io/about?q=hello
https://user:password@www.backtony.github.io

# 다른 origin
http://www.backtony.github.io # 스킴이 다름
https://cktony.github.io # 호스트가 다름
```



## SOP - 동일 출처 정책
웹에는 크게 SOP(Same Origin Policy)와 CORS(Cross Origin Rescure Sharing) 두 가지 정책이 있습니다. SOP는 **같은 출처(Origin)에서만 리소스를 공유할 수 있다**라는 규칙을 갖는 보안 정책입니다. 앞서 언급한 출처(Origin)이 같다면 같은 출처로 인정되고, 자신의 출처와 다를 경우 브라우저는 교차출처 요청을 실행합니다.  

예를 들어 봅시다. 프론트엔드 개발자가 React로 개발하고 백엔드 개발자는 spring으로 개발하는 경우, 백엔드 서버와 프론트가 별도로 존재합니다. 프론트 URL이 http://localhost:3000 이고, 백엔드 URL이 http//localhost:8080 이라면 프론트와 백엔드는 서로 다른 출처(Origin)으로써 SOP를 위반하기 때문에 서버로부터 응답이 브라우저로 넘어갈때 브라우저에서 CORS policy 오류를 발생시킵니다.  

정리하면, scheme, host, port 모두 일치해야 Same Origin(같은 출처)이며, 이들 중 하나라도 일치하지 않으면 Corss Origin이 됩니다.


<Br>


## CORS - 교차 출처 리소스 공유
CORS는 Cross-Origin Resource Sharing(교차 출처 리소스 공유)의 줄임말로, 한 출처에서 실행 중인 웹 애플리케이션이 다른 출처의 선택한 자원에 접근할 수 있는 권한을 부여하도록 브라우저에 알려주는 체제를 의미합니다. 웹 애플리케이션은 리소스(데이터를 가저오는 곳)가 자신의 출처(도메인, 프로토콜, 포트)와 다를 때 교차 출처 HTTP 요청을 실행하게 됩니다.  

<Br>

## CORS 동작 방식
![그림2](./2.png)

웹 애플리케이션은 다른 출처 리소스를 요청할 때, HTTP 프로토콜을 사용하는데 브라우저는 요청 헤더에 Origin 필드로 요청을 보내는 출처를 함께 담아 보냅니다.

<br>

![그림3](./3.png)


서버는 요청에 대한 응답을 보낼 때, 응답 헤더의 Access-Control-Allow-Origin 헤더에 **리소스 접근에 허용된 출처**를 내려주고, 이후 응답을 받은 브라우저는 보냈던 요청의 Origin과 서버 응답의 Access-Control-Allow-Origin 헤더 값을 비교하여 이 응답이 유효한 응답인지 아닌지를 결정합니다. 기본적인 흐름은 위와 같지만, CORS가 동작하는 방식은 한 가지가 아니라 세 가지의 시나리오에 따라 변경됩니다.

<br>


![그림4](./4.png)

CORS는 브라우저의 구현 스펙에 포함되는 정책이기 때문에, 브라우저를 통하지 않고 서버 간 통신을 할 때는 이 정책이 적용되지 않습니다. 예를 들어, spring 서버 안에서 restTemplate을 사용해서 다른 서버로 요청을 보내는 것은 CORS가 발생하지 않습니다. 그리고 일반적으로 CORS 정책을 위반하는 리소스 요청을 하더라도 해당 서버는 정상적으로 응답을 하고, 이후 브라우저가 응답을 분석해서 CORS 정책 위반이라고 판단되면 응답을 사용하지 않고 버립니다. 이는 실제로 서버가 아닌 브라우저에 구현된 스펙이기 때문입니다.  

<br>


### Preflight Request(예비 요청)
실제 요청을 보내기 전에 사전에 예비 요청을 보내고 허용 된다면 실제 요청을 보내는 방식입니다. 사전 요청을 보낼 때는 HTTP 메서드 중 Option 메서드를 사용합니다. 기본적으로 본 요청의 HTTP 메서드가 PUT, DELETE 일 경우 사용됩니다. put이나 delete는 서버의 데이터를 변경하는 요청이기 때문에, 요청을 보내기 전에 예비 요청을 보내서 우선 인증부터 하고 본 요청을 받아 서버에서 코드가 동작하게 하는 원리입니다.


![그림5](./5.png)


1. js로 fetch API를 사용하여 브라우저에게 리소스를 받아오라는 명령을 내립니다.
2. 브라우저는 서버로 HTTP Method Option으로 예비 요청을 보냅니다.
3. 서버는 예비 요청에 대한 응답으로 Access-Control-Allow-Origin 헤더를 통해 현재 서버는 어떤 출처를 허용하고 있는지 알려줍니다.
4. 브라우저는 자신이 보낸 예비 요청과 서버가 응답에 담아준 허용 정책을 비교한 후, 이 요청을 보내는 것이 안전하다고 판단되면 같은 엔드포인트로 다시 본 요청을 보냅니다.
5. 이후 서버가 본 요청에 대한 응답을 주면서 브라우저는 최종적인 응답 데이터를 자바스크립트에게 넘겨줍니다.

<br>

![그림6](./6.png)

option의 요청 헤더들을 보면 단순히 Origin에 대한 정보 뿐만 아니라 자신이 예비 요청 이후에 보낼 본 요청에 대한 다른 정보들도 함께 포함되어 있는 것을 볼 수 있습니다. 위 예비 요청에서는 브라우저는 Access-Control-Requeset-Method를 통해 이후 Delete 메서드를 사용할 것을 서버에 미리 알려주고 있습니다.


<br>

![그림7](./7.png)


이번에는 예비 요청인 Option 메서드의 응답 헤더를 봅시다. Access-Control-Allow-Origin 헤더는 http://localhost:3000 으로 localhost:3000(외부 출처)에서 해당 서버로의 접근은 허용한다는 것입니다.

<br>

### 단순 요청(Simple Request)
![그림8](./8.png)

단순 요청은 예비 요청(Preflight) 없이 바로 본 요청을 서버로 보낸 후, 서버가 이에 대한 응답의 헤더에 Access-Control-Allow-Origin 헤더를 보내주면 브라우저가 CORS 정책 위반 여부를 검사하는 방식입니다. 단순 요청은 예비요청을 아래 3가지 경우를 만족할 때만 가능합니다.

+ 요청 메서드는 GET, HEAD, POST 중 하나여야만 합니다.(Put, Delete는 무조건 Preflight)
+ 유저 에이전트가 자동으로 설정한 헤더 외에, 수동으로 설정할 수 있는 헤더는 Fetch 명세에서 `CORS-safelisted request-header`로 정의되어 있는 다음 헤더만 사용할 수 있습니다.
    - Accept, Accept-Language, Content-Language, Content-Type, DPR, Downlink, Save-Data, Viewport-Width, Width
+ Content-Type을 사용하는 경우에 다음 값만 허용됩니다.
    - application/x-www.form-urlencoded
    - multipart/form-data
    - text/plain

위처럼 조건들이 까다롭기 때문에 단순요청을 보내는 것은 쉽지 않습니다. POST, GET 요청이더라도 대부분 application/json으로 통신하기 때문에 3번째 content-type 조건에 위반됩니다. 따라서 대부분 예비 요청방식으로 이뤄지게 됩니다.

<br>

### 인증된 요청(Credentialed Request)
예비요청에서 보안을 더 강화하고 싶을 때 사용하는 방법입니다. 인증된 요청 역시 예비 요청처럼 Preflight가 먼저 일어납니다. 기본적으로 브라우저가 제공하는 비동기 리소스 요청 API인 XMLHttpRequest 객체나 fetch API는 별도의 옵션 없이 브라우저의 쿠키 정보나 인증과 관련된 헤더를 함부로 요청에 담지 않습니다. 따라서 요청에 인증과 관련된 정보(쿠키)를 담을 수 있게 해주는 옵션이 있는데 credentials 옵션으로 3가지가 있습니다.

- same-origin(기본값) : 같은 출처 간 요청에만 인증 정보를 담을 수 있다.
- include : 모든 요청에 인증 정보를 담을 수 있다.
- omit : 모든 요청에 인증 정보를 담지 않는다.
- fetch를 사용하지 않고 axios를 사용할 땐 withCredentials:true 옵션을 사용하여 쿠키를 전송할 수 있다.

만약 credentials 옵션값인 same-origin이나 include와 같은 옵션을 사용하여 리소스 요청에 인증 정보가 포함된다면, 브라우저는 다른 출처의 리소스를 요청할 때 `Access-Control-Allow-Origin`만 확인하는 것이 아니라 다른 조건을 추가로 검사하게 됩니다. 예를 들어 credentails 옵션을 사용하여 요청에 인증정보가 담겨있는 상태에서 다른 출처의 리소스를 요청하게 되면, 브라우저는 CORS 정책 위반 여부를 검사하는 룰에 다음 두가지를 추가합니다.

+ Access-Controll-Allow-Origin에는 모든 요청을 허용하는 *를 사용할 수 없으며 명시적인 URL이 사용되어야 한다.
+ 응답 헤더에는 반드시 Access-Control-Allow-Credentials: true가 존재해야 한다.

<Br>

## CORS 관련 HTTP Header 정리
+ request Header
  + Origin : cross-site 접근 요청 혹은 사전 전달 요청 출처
  + Access-Control-Request-Method : preflight 사전 요청에서 본 요청 때 어떤 HTTP method를 사용할 것인지 서버에 알리기 위한 것
  + Access-Control-Request-Headers : 사전 요청에서 본 요청 때 어떤 HTTP header가 사용될 것인지 서버에 알리기 위한 것
+ response Header
  + Access-Control-Allow-Origin : origin는 리소스에 접근 가능케 하는 URI를 의미한다.
  + Access-Control-Expose-Headers : cross-origin 요청에 대한 응답인 경우, 해당 응답의 헤더 중에서 브라우저의 스크립트(Java Script...등)가 접근가능한 헤더를 지정하는데 사용
  + Access-Control-Allow-Credentials : credentials 사용 자격 요건에 충족되었는지 여부로 사용되거나 사전 요청에 대한 응답으로 본 요청을 수행할지 여부를 판단
  + Access-Control-Allow-Methods : 허용되는 Method
  + Access-Control-Allow-Headers : 클라이언트에서 서버로 보낼 수 있는 허용되는 헤더



<Br>


## Nginx로 CORS 해결하기
cors 정책에 따라 브라우저는 요청 헤더에 origin이라는 필드에 요청을 보내는 출처를 함께 담아보냅니다. 서버는 응답에서 access-control-allow-origin 헤더에 접근을 허용하는 출처를 응답하고 브라우저는 자신이 보낸 요청의 origin과 서버에서 내려준 access-control-allow-origin을 비교해보고 응답이 유효한지 아닌지 판단합니다. 이를 이용해 처리해봅시다.

cors에 관한 내용들을 담은 파일들을 별도로 만들고 nginx.conf 설정에서 필요한 곳에서 include하는 형식으로 처리해보겠습니다.

**cors-header.conf**
```sh
# proxy_hide_header 옵션은 백엔드 서버에서 응답한 헤더를 제거하는 역할
# 서버에서 내려온 cors 설정 헤더들을 숨겨버린다. -> nginx에서 설정할 것이기 때문
proxy_hide_header Access-Control-Allow-Origin;
proxy_hide_header Access-Control-Allow-Credentials;
proxy_hide_header Access-Control-Allow-Headers;

# add_header는 응답 헤더를 추가해주는 역할
# nginx에서 응답에 cors에 필요한 헤더를 넣어줍니다.
# always 옵션은 모든 조건의 응답에 대해 이 응답 헤더를 포함한다는 의미이다. 이 설정을 하지 않을 경우, 2XX에 해당하는 성공 응답에만 응답 헤더가 포함됩니다.
add_header 'Access-Control-Allow-Origin' $allow_origin always;
add_header 'Access-Control-Allow-Credentials' 'true' always;
add_header 'Access-Control-Allow-Headers' $http_access_control_request_headers always;
```

**cors-options-response.conf**
```sh
if ($request_method = 'OPTIONS') {
    add_header 'Access-Control-Allow-Origin' $allow_origin;
    add_header 'Access-Control-Allow-Credentials' 'true';

    # preflight에 access-control-request-headers 헤더 값 그대로 반환
    # https://www.geeksforgeeks.org/http-headers-access-control-request-headers/
    add_header 'Access-Control-Allow-Headers' $http_access_control_request_headers;
    add_header 'Access-Control-Allow-Methods' 'GET,HEAD,POST,PUT,DELETE';
    add_header 'Access-Control-Max-Age' 86400;
    add_header 'Content-Length' 0;

    return 204;
}
```
요청 메서드가 option이면 preflight 요청이므로 실제 서버로 요청을 보내지 않고 nginx에서 자체적으로 처리하도록 하는 설정입니다. allow_origin 변수는 nginx.conf 파일에서 허용할 origin을 명시할 예정입니다.

**nginx.conf**
기타 옵션들은 제외하고 cors를 적용하기 위한 설정만 작성했습니다.
```sh
worker_processes  1;

events {
    worker_connections  1024;
    use epoll;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    charset utf-8;

    upstream cors_service {
        server localhost:8080;
        keepalive 100;
    }

    # cors에서 허용할 url들 명시해서 매칭되면 origin_allowed 변수를 1로 세팅
    map $http_origin $origin_allowed {
        default 0;
        http://localhost:3000 1;
    }

    # origin_allowed가 1이면 허용된 url이므로 allow_origin 변수에 현재 요청으로 들어온 host_origin 값을 세팅
    # cors-options-response.conf 파일에서 사용된 allow_origin 변수가 여기서 세팅되는 것
    map $origin_allowed $allow_origin {
        default "";
        1 $http_origin;
    }

    server {
        listen       80 default_server;

        # for console!
        access_log /dev/stdout combined;
        error_log /dev/stderr info;

        location / {
            include common/cors-header.conf;
            include common/cors-options-response.conf;

            proxy_pass http://cors_service;
        }
    }
}
```
