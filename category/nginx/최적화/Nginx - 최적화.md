## nginx 설정 최적화
여러번 사용될 수 있는 설정은 각각의 location 블록에서 재사용할 수 있도록 별도의 파일로 분리하는 것이 좋습니다. proxy.conf 파일을 만들고 location 블록마다 include 옵션으로 불러와서 사용할 수 있습니다.

### proxy.conf
```sh
proxy_set_header Host              $host; 
proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header X-Forwarded-Host  $host;
proxy_set_header X-Real-IP         $remote_addr;

# 외부 Analytics를 위한 header setting
add_header Referrer-Policy unsafe-url;

proxy_set_header Connection        "";
proxy_http_version        1.1;

proxy_redirect            off;
proxy_read_timeout        60s;
proxy_ignore_client_abort on;

# 두 옵션은 고려가 필요
proxy_buffering           on;
proxy_max_temp_file_size  0;
```
+ proxy_set_header Host $host
    - 뒷단 서버로 전달되는 요청의 host HTTP 헤더는 기본값으로 구성 파일에 지정한 프록시의 호스트명입니다.
    - 이 설정은 엔진엑스가 클라이언트 요청의 원래 host 값을 대신 사용하도록 설정합니다.
+ proxy_set_header X-Real-IP $remote_addr
    - 뒷단 서버가 엔진엑스에서 오는 요청을 수신하는 이상, 통신하는 IP 주소가 클라이언트 것이 아닙니다.
    - 이 설정을 사용해서 클라이언트의 실제 ip주소를 x-real-ip라는 새로운 헤더에 담아서 전달합니다.
+ proxy_set_header X-forwared-For $proxy_add_X_forwared_for
    - X-Real_IP와 비슷하지만, 클라이언트가 이미 스스로 프록시를 사용하고 있다면 클라이언트의 실제 IP주소는 X-Forwared_For 라는 요청 헤더에 들어있을 것입니다.
    - 통신에 사용하는 소켓과 (프록시 뒤에 있는)클라이언트의 원래 IP 주소 모두 뒷단 서버에 전달하는데 $proxy_add_x_forwared_for을 사용합니다.
+ X-Forwarded-Proto
    - client에서는 nginx로 https 요청을 보내지만 프록시는 서버로 http로 요청을 보내기 때문에 해당 해더로 client 요청 schema가 https인지 http 인지 정보를 넘겨줄 수 있습니다.  
+ Connection, proxy_http_version
    - nginx는 upstream 서버로 proxy를 할 때 HTTP 버전을 1.0으로, Connection 헤더를 close로 변경해서 전달합니다.
    - connection을 유지하기 위해서는 HTTP 버전은 1.1로, connection 헤더는 없애줘야 합니다.
+ proxy_ignore_client_abort
    - on으로 설정 시, 클라이언트가 요청을 중지시켜도 계속해서 프록시 요청을 처리합니다.
+ proxy_redirect
    - off로 설정 시, 리다이렉션에 대해 Location 헤더를 재작성하는 기능을 off 시킵니다.
+ proxy_read_timeout
    - 읽기 작업용 제한 시간을 정한다.
+ proxy_buffering
    - Nginx를 리버스 프록시로 사용할 때, 클라이언트와 서버 사이의 데이터 전송 속도 차이를 극복하기 위해 사용하는 옵션
    - on
        - nginx가 백엔드 서버에서 전체 응답을 수신하고 버퍼링 한 후 클라이언트에 전송
        - 백엔드 서버와의 연결이 빨리 종료되어 서버의 리소스를 절약 가능
        - 클라이언트가 데이터를 천천히 받더라도 Nginx가 모든 데이터를 빠르게 받기 때문에 전체 성능이 향상
    - off
        - nginx가 백엔드 서버의 응답을 클라이언트로 직접 전달
        - 백엔드 서버와 클라이언트 사이의 속도 차이로 인해 백엔드 서버의 리소스가 더 오래 사용될 수 있음
        - 실시간 스트리밍과 같이 지연 시간이 중요한 경우, 이 옵션을 사용하여 데이터를 실시간으로 전송 가능
    - 고려 사항
        - 백엔드 서버의 리소스 절약이 중요 : on 옵션 사용하여 백엔드 서버와의 연결을 빨리 종료할 수 있도록
        - 지연 시간이 중요한 실시간 앱의 경우 : off를 사용하여 nginx가 데이터를 클라이언트에게 직접 전송
+ proxy_max_temp_file_size
    - http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_buffering
    - buffer가 부족하면 파일에 일부를 디스크의 임시 파일에 저장하게 되는데 0으로 설정하면 파일에 쓰지 않겠다는 의미입니다.



### nginx.conf 최적화

```sh
# k8s 클러스터의 물리 장비의 cpu 개수로 판단해서 auto를 사용하지 않고 cpu 코어 개수로 명시
worker_processes 2;

pid /home/backtony/logs/nginx/nginx.pid;

events {
    # reverse proxy의 경우 worker_processes * worker_connections / 4 값은 ulimit -n결과값(open files) 보다 작아야 한다.
    # 총 커넥션은 worker_processes * worker_connections 수가 됨을 유의해서 설정한다. 웹서버 장비에서 `ulimit -n`로 한계를 확인해본다.
    # 보통 위와 같이 계산하기보다는 1024면 충분하다.
    worker_connections 1024;
    # epoll을 사용하면 수천 개의 연결을 제공해야 할 때 CPU 사용량이 줄어든다.
    use epoll;
}

http {
    include      mime.types;
    default_type application/octet-stream;

    charset utf-8;

    client_body_buffer_size     16k;  # POST등의 요청에 포함되는 body에 대한 버퍼 크기 (default : 16k for x86-64)
    client_header_buffer_size   8k;   # 요청 헤더에 대한 버퍼 크기 (default : 1k) - 토큰이나 그런 값들을 고려해서 설정
    client_max_body_size        1m;   # 리퀘스트 body사이즈에 대한 최대 크기 (default : 1m)
    large_client_header_buffers 4 8k; # 긴 URL 요청으로 들어올 수 있는 헤더의 최대크기. 이를 초과할경우 414 응답
    client_header_timeout       5s;   # 클라이언트 요청 후 응답의 헤더 보내기까지 기다리는 시간
    client_body_timeout         5s;   # 클라이언트 요청 후 응답의 바디 보내기까지 기다리는 시간
    send_timeout                5s;   # 성공한 요청 사이 대기 시간
    keepalive_timeout           10s;

    gzip            on;
    gzip_comp_level 6;
    gzip_min_length 1000;
    gzip_proxied    expired no-cache no-store private auth;
    gzip_types      text/plain application/json application/javascript application/x-javascript text/xml text/css application/xml;

    server_tokens    off;
    disable_symlinks on;
  
    # 적은 데이터를 real time에 빠르게 보내기 위한 옵션
    # 작은 데이터 패킷 지연 없이 전송
    tcp_nodelay on;

    upstream webapp {
        server 127.0.0.1:8080;
        # 엔진엑스와 백엔드 서버간의 연결 유지하는 기능(클라이언트가 아니라 엔진엑스)
        # 엔진엑스와 백엔드 서버 사이에 여러번 요청을 처리하는 경우에 유용
        # 매번 연결을 새로 만들 필요 없이 기존 연결을 유지한 채로 여러 요청을 처리
        # 즉, 엔진엑스랑 백엔드 서버랑 핸드쉐이킹 작업 없이 커넥션 100개를 유지할 수 있으니 속도를 더 빠르게 할 수 있다.
        keepalive 100;
    }

    proxy_headers_hash_bucket_size 128;

    server {
        listen 80 default_server;

        set $loggable 1;
        if ($http_user_agent ~* "(kube-probe|NGINX-Prometheus-Exporter)") {
            set $loggable 0;
        }

        # for console!
        access_log /dev/stdout combined if=$loggable;
        error_log /dev/stderr info;

        location / {
            return 444;
        }

        location /http_stub_status {
            # 허용할 ip, cluster ip 목록들 명시
            allow 127.0.0.1/32;
            allow 10.0.0.0/8;
            stub_status;
            deny all;
        }
    }

    server {
        listen 80;
        server_name nginx 도메인;

        set $loggable 1;
        if ($http_user_agent ~* "(kube-probe|NGINX-Prometheus-Exporter)") {
            set $loggable 0;
        }

        access_log /home/backtony/logs/nginx/access.log combined if=$loggable;
        error_log /home/backtony/logs/nginx/error.log info;

        access_log /dev/stdout combined if=$loggable;
        error_log /dev/stderr info;

        underscores_in_headers on;

        location /http_stub_status {
            allow 127.0.0.1/32;
            allow 10.0.0.0/8;
            stub_status;
            deny all;
        }

        location / {
            proxy_pass http://webapp;
            include /home/backtony/apps/nginx/conf/proxy.conf;
        }
    }
}
```

**참고**
* https://kwonnam.pe.kr/wiki/nginx/performance
* https://jojoldu.tistory.com/322
* https://gist.github.com/v0lkan/90fcb83c86918732b894
* https://gist.github.com/denji/8359866
* https://www.nginx.com/blog/performance-tuning-tips-tricks/
* https://thoughts.t37.net/nginx-optimization-understanding-sendfile-tcp-nodelay-and-tcp-nopush-c55cdd276765
* https://www.digitalocean.com/community/tutorials/how-to-optimize-nginx-configuration