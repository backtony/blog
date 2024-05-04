# Armeria-kotlin-grpc 서버 구축하기

## RPC (Remote Procedure Call)와 REST

### RPC

네트워크로 연결된 서버 상의 프로시저(함수, 메서드 등)를 원격으로 호출할 수 있는 기능입니다. 코드 상으로는 마치 로컬 함수의 호출과 같지만 실제로는 함수가 원격 서버에서 실행됩니다. 네트워크 통신을 위한 작업 하나하나 챙기기 귀찮으니 통신이나 call 방식에 신경 쓰지 않고 원격지의 자원을 내 것처럼 사용할 수 있다는 의미입니다. IDL(Interface Definication Language) 기반으로 다양한 언어를 가진 환경에서도 쉽게 확장이 가능하며, 인터페이스 협업에도 용이하다는 장점이 있습니다.

-   지원 언어 : C++, Java, Python, Ruby, Node.js, C#, Go, PHP, Objective-C ...

RPC의 핵심 개념은 'Stub(스텁)'이라는 것입니다. 서버와 클라이언트는 서로 다른 주소 공간을 사용하므로, 함수 호출에 사용된 매개 변수를 꼭 변환해줘야 합니다. 변환하지 않는다면 메모리 매개 변수에 대한 포인터가 다른 데이터를 가리키게 되기 때문입니다. 이 변환을 담당하는 게 스텁입니다.

client stub은 함수 호출에 사용된 파라미터의 변환(Marshalling, 마샬링) 및 함수 실행 후 서버에서 전달된 결과의 변환을, server stub은 클라이언트가 전달한 매개 변수의 역변환(Unmarshalling, 언마샬링) 및 함수 실행 결과 변환을 담당하게 됩니다. 이런 Stub을 이용한 기본적인 RPC 통신 과정은 다음과 같습니다.

[##_Image|kage@lFcne/btsGB2v99QC/VkOQ0RzIwOmJL1CcLs7dOk/img.jpg|CDM|1.3|{"originWidth":727,"originHeight":290,"style":"alignCenter"}_##]

1.  IDL(Interface Definition Language)을 사용하여 호출 규약 정의합니다.
  -   함수명, 인자, 반환값에 대한 데이터형이 정의된 IDL 파일을 rpcgen으로 컴파일하면 stub code가 자동으로 생성됩니다.
2.  Stub Code에 명시된 함수는 원시코드의 형태로, 상세 기능은 server에서 구현됩니다.
  -   만들어진 stub 코드는 클라이언트/서버에 함께 빌드합니다.
3.  client에서 stub에 정의된 함수를 사용할 때, client stub은 RPC runtime을 통해 함수 호출하고 server는 수신된 procedure 호출에 대한 처리 후 결과 값을 반환합니다.
4.  최종적으로 Client는 Server의 결과 값을 반환받고, 함수를 Local에 있는 것처럼 사용할 수 있습니다.

### REST

REST는 HTTP/1.1 기반으로 URI를 통해 모든 자원(Resource)을 명시하고 HTTP Method를 통해 처리하는 아키텍처입니다. 자원 그 자체를 표현하기에 직관적이고, HTTP를 그대로 계승하였기에 별도 작업 없이도 쉽게 사용할 수 있다는 장점으로 현대에 매우 보편화되어 있지만 REST에도 한계는 존재합니다. REST는 일종의 스타일이지 표준이 아니기 때문에 parameter와 응답 값이 명시적이지 않습니다. 또한 HTTP 메서드의 형태가 제한적이기 때문에 세부 기능 구현에는 제약이 있습니다.

웹 데이터 전달 format으로 xml, json을 많이 사용합니다. XML은 html과 같이 tag 기반이지만 미리 정의된 태그가 없어(no pre-defined tags) 높은 확장성을 인정받아 이기종간 데이터 전송의 표준이었으나, 다소 복잡하고 비효율적인 데이터 구조 탓에 속도가 느리다는 단점이 있었습니다. 이런 효율 문제를 JSON이 간결한 Key-Value 구조 기반으로 해결하는 듯하였으나, 제공되는 자료형의 한계로 파싱 후 추가 형변환이 필요한 경우가 많아졌습니다. 또한 두 타입 모두 string 기반이라 사람이 읽기 편하다는 장점이 있으나, 바꿔 말하면 데이터 전송 및 처리를 위해선 별도의 Serialization이 필요하다는 것을 의미합니다.

## gRPC

gRPC는 google 사에서 개발한 오픈소스 RPC(Remote Procedure Call) 프레임워크입니다. 이전까지는 RPC 기능은 지원하지 않고, 메세지(JSON 등)를 Serialize 할 수 있는 프레임워크인 PB(Protocol Buffer, 프로토콜 버퍼)만을 제공해 왔는데, google에서 PB 기반 Serizlaizer에 HTTP/2를 결합한 새로운 RPC 프레임워크 탄생시켰습니다.

**HTTP/2**

-   Streaming
-   Header Compression
-   Multiplexing
  -   1.x의 경우 플레인 텍스트에 헤더와 바디 등의 데이터를 한 번에 전송했지만, 2.0부터는 헤더와 데이터를 프레임이라는 단위로 분리하고 다른 스트림에 속하는 각각의 프레임들을 프레임 단위로 하나의 커넥션에 상호 배치하여 목적지에 전달합니다.

http/1.1은 기본적으로 클라이언트의 요청이 올 때만 서버가 응답을 하는 구조로 매 요청마다 connection을 생성해야만 합니다. cookie 등 많은 메타 정보들을 저장하는 무거운 header가 요청마다 중복 전달되어 비효율적이고 속도도 느려집니다. http/2에서는 한 connection으로 동시에 여러 개 메시지를 주고받으며, header를 압축하여 중복 제거 후 전달하기에 1.x에 비해 효율적입니다. 또한, 필요시 클라이언트 요청 없이도 서버가 리소스를 전달할 수도 있기 때문에 클라이언트 요청을 최소화할 수 있습니다.

### ProtoBuf (Protocol Buffer, 프로토콜 버퍼)

Protocol Buffer는 google 사에서 개발한 구조화된 데이터를 직렬화(Serialization)하는 기법입니다.

[##_Image|kage@czOKQW/btsGCuseeeR/WtRJTUBUdvjAnSk69KgDX0/img.png|CDM|1.3|{"originWidth":773,"originHeight":355,"style":"alignCenter"}_##]

직렬화란, 데이터 표현을 바이트 단위로 변환하는 작업을 의미합니다. 위 그림처럼 같은 정보를 저장해도 text 기반인 json인 경우 82 byte가 소요되는데 반해, 직렬화된 protocol buffer는 필드 번호, 필드 유형 등을 1byte로 받아서 식별하고, 주어진 length 만큼만 읽도록 하여 단 33 byte만 필요하게 됩니다.

### Proto File

Proto File에 Protocol Buffer의 기본 정보를 명세하여 메시지를 정의합니다.

**타입 정의**

| Java Type | Proto Type | default value |
| --- | --- | --- |
| int | int32 | 0 |
| long | int64 | 0 |
| float | float | 0 |
| double | double | 0 |
| boolean | bool | false |
| string | string | empty string |
| byte\[\] | bytes | empty bytes |
| collection / List | repeated | empty list |
| map | map | empty map |

Proto Type의 경우, 기본적으로 null값을 허용하지 않습니다. null값을 허용하기 위해서는 별도의 google에서 제공하는 wrapper 타입을 사용해야 합니다.

**메시지 정의**

```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.example.demo.proto.ecommerce.product";

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";
import "google/protobuf/wrappers.proto";

package ecommerce.product;

service ProductInfo {
    rpc getProduct(ProductID) returns (Product);
}

message Product {
    string id = 1;
    string name = 2;
    google.protobuf.StringValue description = 3;
    double price = 4;
    google.protobuf.Timestamp registeredDate = 6;
}

message ProductID {
    string value = 1;
}
```

-   syntax
  -   규약을 명시하는 부분으로 proto version 3의 규약을 따르겠다고 명시합니다.
  -   proto2와 proto3는 지원하는 언어에 차이가 있으며 문법적으로도 차이가 있습니다.
  -   proto2의 경우에는 optional, required를 사용했지만 proto3에서는 deprecated 되었고 repeated만 proto3에서 사용됩니다.
-   java\_multiple\_files
  -   기본 옵션 값은 false
  -   false로 지정할 경우 오직 하나의 .java 파일이 생성되고, top-level 메시지, 서비스, enum에 대해 생성된 모든 자바 클래스(enum 등)는 outer 클래스 내에 중첩됩니다.
  -   true로 지정할 경우 위와 같은 상황에 대해 각각의 .java 파일이 생성됩니다.
-   import
  -   proto Type에서는 기본적으로 null 값을 허용하지 않지만, google에서 제공하는 wrapper 타입을 사용하면 null을 사용할 수 있습니다.
  -   값을 명시하지 않으면 기본적으로 default value가 지정되지만 값을 꺼내서 사용할 때, XXOrNull 혹은 hasXX 함수 등을 사용하여 값이 채워지지 않았는지 확인할 수 있는 방법이 제공됩니다.
  -   Timestamp 타입으로 시간 관련한 필드를 정의할 수 있습니다.
-   java\_package
  -   기본적으로 package 경로로 생성되나 명시하여 생성되는 파일들의 package 경로를 지정할 수 있습니다.
-   package
  -   message type 간의 이름이 겹치는 경우, 구분할 때 사용합니다.
-   service
  -   서비스 인터페이스를 정의합니다.
  -   client는 stub을 사용하여 해당 인터페이스를 호출하고 server에서는 해당 인터페이스를 구현하게 됩니다.
  -   server가 해당 인터페이스를 구현하게 되는데 spring mvc 관점에서 본다면 controller와 유사합니다.
-   message
  -   요청과 응답 타입 메시지를 정의합니다.
  -   메시지에 정의된 필드들은 각각 고유한 번호(Field Tag)를 갖게 되고 encoding 이후 binary data에서 필드를 식별하는 데 사용됩니다.
  -   최소 1부터 536,870,911까지 지정 가능하며, 19000 ~ 19999는 프로토콜 버퍼 구현을 위해 reserved 된 값이므로 사용할 수 없습니다.
  -   필드 번호가 1 ~ 15일 때는 1byte, 16 ~ 2047은 2byte를 Tag로 가져가게 되기 때문에 자주 호출되는 필드에 대해서는 1 ~ 15로 지정하는 것이 권장됩니다.

### 버전 호환성

> [https://protobuf.dev/programming-guides/proto3/#reserved](https://protobuf.dev/programming-guides/proto3/#reserved)  
> [https://stackoverflow.com/questions/60490487/whats-the-best-way-to-deprecate-a-field-in-protocol-buffer-v3-reserved-vs-depre](https://stackoverflow.com/questions/60490487/whats-the-best-way-to-deprecate-a-field-in-protocol-buffer-v3-reserved-vs-depre)

```protobuf
message Product {
  string id = 1;
  string name = 2;
  google.protobuf.StringValue description = 3;
  double price = 4;
  google.protobuf.Timestamp registeredDate = 6 [deprecated=true]; // 필드 제거
  // reserved = 6;
  google.protobuf.Timestamp createdAt = 7; // 새로운 필드 추가
}
```

필드번호는 메시지의 호환성을 유지하는 핵심요소입니다. 제거할 때는 해당 번호를 재사용하지 않도록 주의해야 합니다. 배포 이후에 protobuf에서 필드 제거가 필요한 경우, deprecated를 명시하는 방법과 필드 자체를 제거하는 방법이 있습니다. 해당 필드를 제거하는 경우 reserved 키워드를 사용하여 재사용을 방지할 수 있습니다. 필드를 추가할 경우에는 새로운 번호를 사용해야 합니다.

## Armeria

Armeria는 Line에서 개발한 MSA 프레임워크입니다. 하나의 포트에서 여러 가지 프로토콜(http, gRPC, Thrift)을 사용할 수 있고 gRPC의 문서화를 자동으로 생성해 주는 등 다양한 기능을 제공합니다.

Armeria의 소개 및 장점은 아래 링크를 참고 바랍니다.

-   [공식 홈페이지](https://armeria.dev/)
-   [Armeria 소개 Line Blog](https://engineering.linecorp.com/ko/blog/introduce-armeria)
-   [LINE 개발자들이 Spring 대신 Armeria를 사용하는 이유](https://engineering.linecorp.com/ko/blog/hello-armeria-bye-spring)
-   [Spring WebFlux와 Armeria를 이용하여 Microservice에 필요한 Reactive + RPC 동시에 잡기](https://d2.naver.com/helloworld/6080222)

## 멀티모듈 프로젝트 구축하기

멀티모듈 형태로 Armeria & spring를 사용한 grpc-server와 일반적인 spring web을 사용한 grpc-client 서버를 구축해 보겠습니다. 모듈은 다음과 같습니다.

-   protos : proto file 정의하는 모듈
-   stub : proto file을 rpcgen으로 컴파일하여 stub code를 생성하는 모듈
-   grpc : Armeria & Spring 을 사용한 grpc server 모듈
-   rest-client : grpc-server를 호출하는 spring client 모듈

예시 코드는 member와 team 모듈을 사용하나 포스팅을 간소화하기 위해 member 모듈만 설명하겠습니다. 전체 코드는 [여기](https://github.com/backtony/blog/tree/main/category/armeria%20grpc%20graphql/armeria)를 확인 바랍니다. 예시 코드는 [grpc-kotlin-example](https://github.com/grpc/grpc-kotlin/tree/master/examples)와 [Armeria-example](https://github.com/line/armeria-examples) 를 참고하였습니다.

### protos 모듈

```
└── protos
  ├── build.gradle.kts
  └── src
      └── main
          └── proto
              ├── member
              │ └── member.proto
              └── team
                  └── team.proto
```

protos 모듈의 트리구조는 위와 같습니다.

**member.proto 파일**

```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.example.proto.member";

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";
import "google/protobuf/wrappers.proto";

package member;

service MemberHandler {
    rpc createMember(CreateMemberRequest) returns (MemberResponse);
    rpc getMembersByTeamId(TeamId) returns (MemberListResponse);
}

message CreateMemberRequest {
    string name = 1;
    google.protobuf.StringValue introduction = 2;
    Country country = 3;
    Type type = 4;
    google.protobuf.Int64Value teamId = 5;
    string requestedBy = 6;
}

enum Type {
    UNKNOWN_TYPE = 0;
    INDIVIDUAL = 1;
    COMPANY = 2;
}

enum Country {
    UNKNOWN_COUNTRY = 0;
    KR = 1;
    US = 2;
    JP = 3;
}

message MemberResponse {
    int64 id = 1;
    string name = 2;
    google.protobuf.StringValue introduction = 3;
    Type type = 4;
    Country country = 5;
    google.protobuf.Int64Value teamId = 6;
    string registeredBy = 7;
    google.protobuf.Timestamp registeredDate = 8;
    string modifiedBy = 9;
    google.protobuf.Timestamp modifiedDate = 10;
}

message TeamId {
    int64 id = 1;
}

message MemberListResponse {
    repeated MemberResponse member = 1;
}
```

member를 생성하고 teamId로 member를 조회하는 rpc service를 정의했습니다. enum의 경우 필드 고유 번호가 0이 존재하는데, 아무런 값이 들어오지 않으면 0에 해당하는 enum 값이 default로 설정됩니다.

> repeated 타입의 필드의 경우 변수명을 members(복수)로 지정하게 되면 code gen 된 클래스에서 필드명이 membersList로 지정됩니다. 이러한 이슈 때문에 repeated의 경우 단수로 네이밍하고 있습니다.

**build.gradle.kts**

```groovy
import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

java {
    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}
```

src/main/proto 디렉토리를 main 소스셋의 리소스 디렉토리로 추가합니다. stub모듈에서 해당 모듈을 가져다가 사용할 예정입니다.

### stub 모듈

```
└── stub
    └──  build.gradle.kts
```

stub 모듈의 트리구조는 위와 같습니다.

**build.gradle.kts**

```groovy
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.google.protobuf") version "0.9.4"
}

val grpcKotlinVersion = "1.4.1"
val grpcProtoVersion = "1.63.0"
val grpcVersion = "3.25.3"

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

// https://github.com/grpc/grpc-kotlin/blob/master/examples/stub/build.gradle.kts
dependencies {
    protobuf(project(":protos"))

    api("io.grpc:grpc-stub:$grpcProtoVersion")
    api("io.grpc:grpc-protobuf:$grpcProtoVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion") // kotlin stub 제공
    api("com.google.protobuf:protobuf-kotlin:$grpcVersion") // kotlin 코드 생성 도구
    api("io.grpc:grpc-netty:$grpcProtoVersion") // stub NettyChannel에 사용
}

protobuf {
    // Configure the protoc executable.
    protoc {
        // Download from the repository.
        artifact = "com.google.protobuf:protoc:$grpcVersion"
    }

    // Locate the codegen plugins.
    plugins {
        // Locate a plugin with name 'grpc'.
        id("grpc") {
            // Download from the repository.
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcProtoVersion"
        }
        // Locate a plugin with name 'grpcKt'.
        id("grpckt") {
            // Download from the repository.
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }

    // generate code
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}
```

stub 모듈은 protos 모듈의 proto file을 가져와 proto file 정의에 따른 java, kotlin stub 코드들을 만들어내는 모듈입니다. 해당 세팅에서 build를 하면 build/generated/source/proto/main 경로에 proto file에 정의된 형태의 코드들이 생성됩니다.


### grpc 모듈

grpc 모듈은 Armeria를 사용하여 grpc 서버를 구축합니다. grpc 모듈의 자세한 구조는 [여기](https://github.com/backtony/blog/tree/main/category/armeria%20grpc%20graphql/armeria)를 참고 바랍니다.

#### build.gradle.kts

```groovy
val armeriaVersion = "1.27.0"

dependencies {
    implementation(project(":stub"))

    // armeria
    // https://github.com/line/armeria-examples/blob/main/grpc/build.gradle
    implementation(platform("io.netty:netty-bom:4.1.106.Final"))
    implementation(platform("com.linecorp.armeria:armeria-bom:$armeriaVersion"))
    implementation("com.linecorp.armeria:armeria-kotlin:$armeriaVersion")
    implementation("com.linecorp.armeria:armeria-spring-boot3-starter:$armeriaVersion")
    implementation("com.linecorp.armeria:armeria-spring-boot3-actuator-starter:$armeriaVersion")

    // grpc
    implementation("com.linecorp.armeria:armeria-grpc:$armeriaVersion")

    // r2dbc
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.asyncer:r2dbc-mysql:1.1.0")
}
```

proto file을 사용하여 code gen을 해주는 stub 프로젝트를 추가하고 armeria와 grpc를 위한 의존성을 추가해 줍니다. DI를 사용하기 위해 spring 의존성과 데모 코드에서는 db를 r2dbc-mysql을 사용하므로 r2dbc 관련 의존성도 추가해줍니다.

#### MemberHandler

```kotlin
@GrpcHandler
class MemberHandler(
    private val memberService: MemberService,
) : MemberHandlerGrpcKt.MemberHandlerCoroutineImplBase() {

    override suspend fun createMember(request: CreateMemberRequest): MemberResponse {
        return memberService.createMember(MemberMapper.generateCreateMemberRequest(request))
            .let { MemberMapper.generateMemberResponse(it) }
    }

    // ... 생략
}
```

앞서 member.proto 파일에서 정의했던 **`service MemberHandler`**가 stub 모듈에서 code gen 되면서 MemberHandlerGrpcKt 와 같은 클래스들이 생성됩니다.

**`MemberHandlerGrpcKt.MemberHandlerCoroutineImplBase()`** 추상 클래스의 메서드를 재정의함으로써 grpc service의 구현이 시작됩니다. spring mvc 관점에서 보면 Controller에 해당한다고 볼 수 있습니다. 함수의 인자로 사용되는 request와 response 모두 proto file에 정의해 두었던 message가 stub 모듈에서 code gen 되면서 생성된 클래스들입니다. @GrpcHandler 애노테이션은 단순 마킹용 애노테이션으로 grpc의 구현체라는 것을 명시하기 위해서 달아두었습니다.

```kotlin
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class GrpcHandler
```

#### MemberMapper와 ProtoTypeUtil

```kotlin
object MemberMapper {

    fun generateCreateMemberRequest(request: CreateMemberRequest): MemberDto.CreateMemberRequest {

        return with(request) {
            MemberDto.CreateMemberRequest(
                name = name,
                introduction = introductionOrNull?.value, // google.protobuf
                type = Member.Type.valueOf(type.name),
                country = Member.Country.valueOf(country.name),
                teamId = teamIdOrNull?.value, // google.protobuf
                requestedBy = requestedBy,
            )
        }
    }

    fun generateMemberResponse(member: Member): MemberResponse {

        return memberResponse {
            id = member.id!!
            name = member.name
            member.introduction?.let {
                introduction = StringValue.of(it) // google.protobuf
            }
            type = Type.valueOf(member.type.name)
            country = Country.valueOf(member.country.name)
            member.teamId?.let {
                teamId = Int64Value.of(it) // google.protobuf
            }
            registeredBy = member.registeredBy
            registeredDate = member.registeredDate.toTimestamp() // google.protobuf
            modifiedBy = member.modifiedBy
            modifiedDate = member.modifiedDate.toTimestamp() // google.protobuf
        }
    }

    fun generateMemberListResponse(members: List<Member>): MemberListResponse {
        return memberListResponse {
            member.addAll(members.map { generateMemberResponse(it) }) // repeated field
        }
    }
}
```

MemberMapper는 code gen 된 클래스와 내부 Dto로 혹은 도메인으로 변환하는 매퍼클래스입니다. code gen으로 생성된 클래스의 경우 코틀린 dsl이 제공되므로 이를 사용하여 더 간결하게 구현할 수 있습니다. protobuf를 정의할 때, google.protobuf를 사용한 경우 XXXorNull 메서드를 사용하여 값이 들어오지 않은 경우 null값을 꺼낼 수 있으며 반대로 google.protobuf 타입으로 만들기 위해서는 XXValue.of 메서드를 사용할 수 있습니다. 그리고 repeated 타입의 필드의 경우 addAll 메서드를 사용하여 값을 넣어줄 수 있습니다.

protobuf에는 시간 관련된 타입으로 TimeStamp만을 제공하기 때문에 LocalDateTime을 TimeStamp 타입으로 변환하기 위해서 아래와 같이 확장함수를 정의하여 사용할 수 있습니다.

```kotlin
// ProtoTypeUtil
fun LocalDateTime.toTimestamp(): Timestamp {
  return toTimestamp(ZoneId.systemDefault())
}

fun LocalDateTime.toTimestamp(zoneId: ZoneId): Timestamp {
  val instant = this.atZone(zoneId).toInstant()
  return Timestamp.newBuilder()
    .setNanos(instant.nano)
    .setSeconds(instant.epochSecond)
    .build()
}
```

#### Interceptor

grpc interceptor는 크게 serverInterceptor와 clientInterceptor로 구분되며 각 구분의 하위로 streaming과 unary로 다시 분류됩니다.

```java
@ThreadSafe
public interface ServerInterceptor {

  <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next);
}
```

ServerInterceptor 인터페이스는 interceptCall 단일 메서드만 가지고 있습니다. 이 메서드는 클라이언트로부터의 각 호출에 대해 실행되며, 인터셉터 체인을 통해 다음 인터셉터 또는 실제 서비스 메서드로 요청을 전달합니다. ServerCall은 클라이언트로부터 받은 RPC(원격 프로시저 호출) 요청을 나타냅니다. 이 객체를 통해 서버는 클라이언트에게 응답을 보낼 수 있습니다.

```java
public interface ServerCallHandler<RequestT, ResponseT> {

  ServerCall.Listener<RequestT> startCall(
      ServerCall<RequestT, ResponseT> call,
      Metadata headers);
}
```

ServerCallHandler는 요청을 처리하는 로직을 캡슐화합니다. 이 핸들러는 serverCall, Metadata를 인자로 받아 요청에 대한 실제 비즈니스 로직을 수행합니다. 요청을 처리하면서 ServerCall.Listener 객체를 생성하고 반환하는데 이 리스너는 클라이언트로부터 추가적인 메시지를 수신하거나, 요청 처리가 반쪽 받힘 상태, 요청 완료되었을 때 등의 다양한 이벤트를 처리하는 콜백 메서드를 제공합니다.

```kotlin
class TestInterceptor : ServerInterceptor {

    private val log = KotlinLogging.logger {  }

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): Listener<ReqT> {

        // 전처리
        log.info("pre handle")

        return next.startCall(call, headers)
    }
}
```

인터셉터에서 전처리는 serverCallHandler의 startCall 메서드를 호출하기 전에 수행할 수 있습니다. 후처리는 두 가지 방법으로 수행할 수 있습니다.

-   serverCall 재정의
-   listener 재정의

SimpleForwardingServerCall과 SimpleForwardingServerCallListener는 각각 ServerCall과 ServerCall.Listener의 편리한 래퍼 클래스로, 이 래퍼들은 gRPC 서버에서 요청을 다루는 데 필요한 메서드를 상속받아, 개발자가 특정 메소드를 오버라이드하는 것을 간소화합니다. 이를 활용하여 아래와 같이 간단한 로깅 인터셉터를 구현할 수 있습니다.

```kotlin
class SimpleLoggingInterceptor : ServerInterceptor {

  override fun <ReqT : Any?, RespT : Any?> interceptCall(
    call: ServerCall<ReqT, RespT>,
    headers: Metadata,
    next: ServerCallHandler<ReqT, RespT>,
  ): Listener<ReqT> {
    val serverCall = LoggingServerCall(
      delegate = call,
      startCallMillis = System.currentTimeMillis(),
    )

    return LoggingServerCallListener(next.startCall(serverCall, headers))
  }

  class LoggingServerCall<ReqT, RespT>(
    private val delegate: ServerCall<ReqT, RespT>,
    private val startCallMillis: Long,
  ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(delegate) {

    override fun close(status: Status, trailers: Metadata?) {
      log.info {
        "status:${status.code.name} " +
                "rpc:${delegate.methodDescriptor.fullMethodName.replace("/", ".")} " +
                "responseTime:${(System.currentTimeMillis() - startCallMillis)}ms "
      }
      super.close(status, trailers)
    }
  }

  class LoggingServerCallListener<ReqT>(
    delegate: Listener<ReqT>,
  ) : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

    override fun onMessage(message: ReqT) {
      log.info("Receive Message : ${message.toString().trim()}")
      super.onMessage(message)
    }
  }

  companion object {
    private val log = KotlinLogging.logger { }
  }
}
```

SimpleForwardingServerCall과 SimpleForwardingServerCallListener를 재정의하여 이외의 다양한 지점에서 로직을 구현할 수 있습니다.

[##_Image|kage@cPEMya/btsGAZttzph/h0yMHRu02RS2MysjEdPUpk/img.png|CDM|1.3|{"originWidth":1100,"originHeight":1200,"style":"alignCenter"}_##]

> [https://grpc.github.io/grpc-java/javadoc/io/grpc/ForwardingClientCall.html](https://grpc.github.io/grpc-java/javadoc/io/grpc/ForwardingClientCall.html)  
> [https://grpc.github.io/grpc-java/javadoc/io/grpc/ClientCall.Listener.html](https://grpc.github.io/grpc-java/javadoc/io/grpc/ClientCall.Listener.html)

자세한 내용은 위 문서를 참고 바랍니다.

#### 예외 처리

Spring에서는 @ControllerAdvice로 전체적인 예외처리를 담당했다면 grpc에서는 interceptor를 사용하여 처리할 수 있습니다.

```kotlin
class GlobalExceptionInterceptor : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {

        return next.startCall(ExceptionServerCall(call), headers)
    }

    class ExceptionServerCall<ReqT, RespT>(
        delegate: ServerCall<ReqT, RespT>,
    ) : SimpleForwardingServerCall<ReqT, RespT>(delegate) {

        override fun close(status: Status, trailers: Metadata?) {
            if (status.isOk) {
                super.close(status, trailers)
            } else {
                val exceptionStatus: Status = handleException(status.cause)
                log.error("gRPC exception : \n$exceptionStatus", status.cause)
                super.close(exceptionStatus, trailers)
            }
        }

        /**
         * Exception을 grpc error Code로 변경
         */
        private fun handleException(e: Throwable?): Status {
            when (e) {
                is IllegalArgumentException -> return Status.INVALID_ARGUMENT.withDescription(e.message)
            }

            return Status.INTERNAL.withDescription(e?.message)
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
```

grpc는 http status code와 달리 별도의 status 코드를 사용합니다. 관련 코드는 [공식 문서](https://grpc.io/docs/guides/status-codes/)를 참고 바랍니다.

#### Armeria Config

ArmeriaConfig 클래스는 하나씩 쪼개서 살펴보겠습니다.

```kotlin
@Configuration
class ArmeriaConfig {

    @Bean
    fun grpcService(
        allServiceBean: List<AbstractCoroutineServerImpl>,
    ): GrpcService {
        val grpcServiceBuilder = GrpcService.builder()
            .enableUnframedRequests(true)
            .intercept(SimpleLoggingInterceptor(), GlobalExceptionInterceptor())

        allServiceBean.forEach {
            logger.info("Register Grpc Bean : {}", it.javaClass.name)
            grpcServiceBuilder.addService(it)
        }

        return grpcServiceBuilder.build()
    }
}
```

앞서 grpc의 진입점에 해당하는 service는 proto file의 service code gen으로 생성된 XX.XXImpleBase 클래스를 상속받아 구현했습니다. [공식 문서](https://armeria.dev/docs/server-grpc)에 따르면 각각의 서비스마다 등록하도록 가이드가 되어있지만 서비스가 많아질수록 이는 번거로운 작업이므로 빈으로 등록해 두고 ArmeriaServerConfigurator에 등록해주려고 합니다. XXXImplBase는 AbstractCoroutineServerImpl를 상속받기 때문에 이를 활용하여 인자로 구현체들을 주입받아 grpcServiceBuilder에 모든 서비스를 추가하고, 앞서 만들어주었던 인터셉터도 등록합니다.

```java
@FunctionalInterface
public interface ArmeriaServerConfigurator extends Ordered {
  // https://javadoc.io/doc/com.linecorp.armeria/armeria-javadoc/latest/com/linecorp/armeria/server/ServerBuilder.html
  void configure(ServerBuilder serverBuilder);
}
```

마지막으로 위 인터페이스를 구현하여 armeria 설정을 마무리합니다. serverBuilder에는 다양한 옵션이 있으므로 옵션을 추가하고자 한다면 위 주석 링크를 참고 바랍니다.

```kotlin
@Configuration
class ArmeriaConfig {

    @Bean
    fun armeriaServerConfigurator(
        grpcService: GrpcService,
    ): ArmeriaServerConfigurator {        

        return ArmeriaServerConfigurator {

            // Max Request Length 증설
            it.maxRequestLength(32 * 1024 * 1024)

            // Grpc 사용을 위한 서비스 등록 
            it.service(grpcService)

            // Docs 생성을 위한 서비스 등록
            // /docs 경로에 대해서 DocService를 등록 
            it.serviceUnder("/docs", DocService())

            // https://armeria.dev/docs/server-decorator
            // Logging을 위한 Decorator 등록
            it.decorator(LoggingService.newDecorator())
//            it.decorator(LoggingService.builder()
//                .requestLogLevel(LogLevel.INFO)  // 요청 로그 레벨 설정
//                .successfulResponseLogLevel(LogLevel.INFO)  // 성공 응답 로그 레벨 설정
//                .failureResponseLogLevel(LogLevel.ERROR)  // 실패 응답 로그 레벨 설정
//                .newDecorator()
//            )
        }
    }
}
```

-   DocService : swagger처럼 web상에서 grpc 테스트를 손쉽게 할 수 있는 기능을 제공합니다.
-   Decorator : Armeria에서는 들어오는 요청이나 나가는 응답을 가로채기 위해 다른 서비스를 데코레이팅 서비스 또는 데코레이터를 제공합니다. 이름에서 알 수 있듯이 데코레이터 패턴을 구현한 것입니다.
  -   LoggingService는 로깅을 제공합니다. `com.linecorp.armeria.server.logging.LoggingService: DEBUG` 로 로그레벨을 지정해 주면 요청과 응답에 대해 로깅이 되며, 로그레벨을 yml에 지정하지 않고 위의 주석처럼 명시해 줄 수도 있습니다.

### rest-client 모듈

```groovy
dependencies {
    implementation(project(":stub"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
}
```

rest-client 모듈은 grpc-server을 호출하는 client 모듈입니다. grpc 호출을 위해 stub을 추가해줍니다.

#### channel
```kotlin
@Configuration
class GrpcChannelConfig(
    private val grpcProperties: GrpcProperties,
) {

    @Bean
    fun grpcChannel(): ManagedChannel {
        return NettyChannelBuilder.forAddress(grpcProperties.endpoint, grpcProperties.port)
            .usePlaintext()
            .build()
    }
}
```
gRPC는 커넥션, 커넥션풀, 로드밸런싱 등을 추상화하고 있는 channel을 제공합니다. channel은 서버로의 연결을 관리하며 클라이언트가 RPC를 서버에 호출할 때 사용됩니다. ManagedChannel은 연결 설정, 유지 통신 중 오류 처리, 연결 종료와 같은 작업을 자동으로 처리하는 기능을 제공합니다. 그리고 channel은 클라이언트 스텁을 생성할 때 사용됩니다.


#### StubFactory
Client는 grpc-server를 호출하기 위해서 stub을 만들어 사용합니다.

```kotlin
@Component
class MemberStub(
    private val stubFactory: StubFactory
) {

    @Bean
    fun memberServiceStub(): MemberHandlerGrpcKt.MemberHandlerCoroutineStub {
        return stubFactory.createStub(MemberHandlerGrpcKt.MemberHandlerCoroutineStub::class)
    }
}
```
```kotlin
@Component
class StubFactory(
    private val grpcProperties: GrpcProperties,
    private val grpcChannel: ManagedChannel,
) {

    fun <T> createStub(
        stubClass: KClass<T>,
        timeout: Long = grpcProperties.timeout,
    ): T where T : AbstractCoroutineStub<T> {
        val constructor = stubClass.primaryConstructor!!
        return constructor.call(grpcChannel, CallOptions.DEFAULT)
            .withInterceptors(TimeoutInterceptor(timeout))
//            .withDeadlineAfter(3, TimeUnit.SECONDS)      
    }
}
```
하나씩 stub을 만들어서 빈으로 등록해줘도 무관하나 StubFactory 클래스를 만들어서 stub 생성을 한곳에서 처리하여 공통화하고 중복을 줄입니다. reflection을 사용해서 만들어주고 timeout을 설정하는 interceptor를 추가한 stub을 생성해서 반환하는 메서드를 제공합니다. 기본적으로 stub은 withDeadlineAfter라는 메서드로 timeout을 지정할 수 있으나 withDeadlineAfter을 사용해서 만들어진 stub 인스턴스는 만들어진 순간부터 timeout 카운트가 진행됩니다. 따라서 withDeadlineAfter 메서드 대신 timeout을 처리하는 interceptor를 추가해주었습니다.

```kotlin
class TimeoutInterceptor(
    private val timeout: Long,
) : ClientInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {

        return next.newCall(method, callOptions.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS))
    }
}
```

client interceptor는 serverInterceptor와 크게 차이가 나지 않으며 다음과 같이 동작합니다.

[##_Image|kage@lzqmc/btsGDzmigwO/ioSXYM1oABnntMAaketsz1/img.png|CDM|1.3|{"originWidth":696,"originHeight":800,"style":"alignCenter"}_##]

#### MemberClient

```
@Component
class MemberClient(
    private val memberServiceStub: MemberHandlerGrpcKt.MemberHandlerCoroutineStub,
) {
    suspend fun createMember(request: MemberDto.CreateMemberRequest): MemberDto.MemberResponse {
        return memberServiceStub.createMember(MemberMapper.generateCreateMemberRequest(request))
            .let { MemberMapper.generateMemberResponse(it) }
    }

    suspend fun getMembersByTeamId(teamId: Long): MemberDto.MemberListResponse {
        return memberServiceStub.getMembersByTeamId(MemberMapper.generateTeamId(teamId))
            .let { MemberMapper.generateMemberListResponse(it) }
    }
}
```

실제로 호출 시에는 위와 같이 사용할 수 있습니다.



**참고**

-   [시대의 흐름, gRPC 깊게 파고들기](https://blog.naver.com/n_cloudplatform/221751268831)
-   [Introduction to Java gRPC Interceptor](https://engineering.kabu.com/entry/2021/03/31/162401)
-   [Armeria 공식 문서](https://armeria.dev/docs/server-grpc)
-   [grpc 공식 문서](https://grpc.io/docs/what-is-grpc/)



