## 실행 방법
1. dockerfile/zipkin 디렉토리에서 docker-compose up 명령어로 zipkin 띄우기
2. grpc application.yml 파일에 r2dbc 정보 변경하고 아래 테이블 생성하기
3. grpc 띄우고, graphql 혹은 rest-client 띄워서 테스트
    * graphql은 http://localhost:7071/graphiql 에서 테스트 가능
    * grpc는 http://localhost:8080/docs 에서 테스트 가능

```kotlin
create table armeria.member
(
    id              bigint auto_increment
        primary key,
    name            varchar(255) not null,
    introduction    varchar(255) null,
    type            varchar(255) not null,
    country         varchar(255) not null,
    team_id         bigint       null,
    registered_by   varchar(255) not null,
    registered_date datetime     not null,
    modified_by     varchar(255) not null,
    modified_date   datetime     not null
);

create table armeria.team
(
    id              bigint auto_increment
        primary key,
name            varchar(255) not null,
registered_by   varchar(255) not null,
registered_date datetime     not null,
modified_by     varchar(255) not null,
modified_date   datetime     not null
);
```



