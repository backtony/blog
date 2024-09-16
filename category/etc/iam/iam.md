> https://musma.github.io/2019/11/05/about-aws-iam-policy.html


## IAM 이란
AWS Identity and Access Management(IAM)은 AWS 리소스에 대한 액세스를 안전하게 제어할 수 있는 웹 서비스입니다. IAM을 사용하면 사용자가 액세스할 수 있는 AWS 리소스를 제어하는 권한을 관리할 수 있습니다. IAM을 사용하여 리소스를 사용하도록 인증(로그인) 및 권한 부여(권한 있음)된 대상을 제어합니다. IAM은 AWS 계정에 대한 인증 및 권한 부여를 제어하는 데 필요한 인프라를 제공합니다.  

IAM에는 다음과 같은 개념들이 있습니다.
* UBAC (User-Based Access Control)
* GBAC (Group-Based Access Control)
* RBAC (Role-Based Access Control)
* ABAC (Attribute-Based Access Control)

<br> 

## UBAC (User-Based Access Control)
접근 제어를 하는 가장 단순한 방법은 사용자에게 직접 권한을 부여하는 방식입니다.

```shell
사용자A1: a 가능
사용자A2: a 가능

사용자B1: b 가능
사용자B2: b 가능
```
<br>

## GBAC (Group-Based Access Control)
사용자에게 직접 권한을 부여하다 보면 같은 권한이 있는 군집이 있는 것을 발견할 수 있습니다. 그래서 사용자에게 직접 권한을 주는 것보다 그룹에다 권한을 주고 사용자를 그룹에 넣는 방식이 Group 베이스 방식 입니다.

```shell
# 사용자 그룹 설정
사용자A1: 그룹A
사용자A2: [그룹A, 그룹D]
사용자B1: [그룹B, 그룹C]
사용자B2: [그룹B, 그룹D]

# 그룹 권한 설정
그룹A: a가능
그룹B: b가능
그룹C: [a가능, c가능]
그룹D: [a가능, c불가, d가능]
```

<br>

## RBAC (Role-Based Access Control)
권한이 다양하게 존재하면 그룹도 점점 다양해지고 권한 검사를 하는데 그룹으로 조건을 거는 것이 어려울 수 있습니다. 예를 들어 '권한 a가 가능한 그룹은 그룹 A, C, D가 있는데 어느 그룹 소속이냐' 식으로 권한을 검사하기 어렵습니다. 그래서 Role(역할)이라는 개념이 도입됩니다.  

그룹에다 권한을 바로 주는 대신 **권한의 논리적 집합**으로서 역할을 만들고 역할을 그룹이나 사용자에게 연결합니다. **group은 user을 묶어주는 역할**을 하고 **role은 권한의 묶음 역할**로 사용하는 것입니다.  

group은 울타리에 비유하고 role은 포스트잇에 비유하면 이해가 쉽습니다. role은 user든 group이든 어디든지 잘 붙습니다.  

권한을 검사할 때 group에 조건을 거는 것보다는 좀 더 보안적인 측면에 가까운 role에 대해 규칙을 설정하는 것이 더 쉽기 때문에 AWS의 많은 서비스에서 권한을 설정할 때는 group 대신 role을 지정하는 부분이 많습니다. 

<br>

## ABAC (Attribute-Based Access Control) = PBAC - Policy-Based Access Control

일반적인 Role base 접근 제어도 세부적인 접근 제어를 하고자 한다면 여전히 한계가 있습니다.

```shell
[사용자]
김진호 중사 (그룹: [7내무반])
김한국 일병 (그룹: [7내무반])

[그룹]
7내무반 (역할: [병기관리, 탄약관리(추가 예정)])

[역할]
병기관리 (권한: [소총 청소])
탄약관리 (권한: [탄약고 개방]) (추가 예정)

[권한]
Allow 소총 청소
Allow 탄약고 개방 (추가 예정)
```
원래 7내무반 그룹에는 병기관리 역할만 있었는데, 새로 탄약관리 역할을 추가하려고 합니다. 하지만 중사 이상의 계급에게만 탄약관리 역할을 부여하고 싶다면 어떻게 해야할 까요? 사용자 수준에 직접 역할을 주면 그룹 수준의 관리가 안되기 때문에 더 복잡해질 가능성이 있습니다.  

만약 탄약관리 역할은 7내무반 그룹에 부여하되, 그것이 사용자 계급이 간부인지, 병사인지에 따라 적용이 달리된다면 어떨까요? 여기서 계급은 사용자가 가진 속성(attribute) 중 하나 입니다. 그리고 역할에 붙은 권한 부여 형식이 지금처럼 단지 뭐를 허용(allow), 불허(deny) 하는 명제 형식이 아니라 **만약 ~하면 ~한다**식의 조건문 형식으로 붙는다면 동적 권한부여가 가능합니다.

```shell
[사용자]
김진호 중사 (그룹: [7내무반])
김한국 일병 (그룹: [7내무반])

[그룹]
7내무반 (역할: [병기관리, 탄약관리])

[역할]
병기관리 (정책: [소총 청소])
탄약관리 (정책: [탄약고 개방])

[정책]
Allow 소총 청소 (조건 없는, 단순 권한 부여)
Allow 탄약고 개방 if 사용자.간부인가? == True (Subject 속성 사용)
Allow 탄약고 개방 if 탄약고.name == '간이 탄약고' (Object 속성 사용)
Allow 탄약고 개방 if 현재시각 BETWEEN 09:00 AND 12:00 (Global/Environment/Context 속성 사용)
```
권한이 정책으로 변경되었습니다. 정책(policy)는 동적이면서 조건적으로 권한을 부여할 수 있게 하는 요소입니다. 속성을 참조하고 반영하기 때문에 ABAC이고 Policy를 열거하기 때문에 PBAC라고도 합니다.  

권한을 부여하는 Policy 형식을 추가하고 role이 여러 policy를 가질 수 있는 구조를 만들어 RBAC 구조를 그대로 유지합니다.  

<br>

### Policy Json 문법
Policy는 Json으로 구성되어있습니다.  

```json
{
  "Version": "2012-10-17",
  "Id": "cd3ad3d9-2776-4ef1-a904-4c229d1642ee",  
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::hello/*"
    }
  ]
}
```
위 형식은 항상 고정입니다. Version과 Statement 속성은 항상 포함됩니다.

* Version
  * String 
  * IAM Policy Json 문서 양식 버전
  * 2008-10-17이 default이지만 2012-10-17이 최신입니다.
* Statement
  * Array
  * 권한 부여 규칙(Rule 혹은 Policy) 나열
* Id
  * Optional
  * UUID
  * 정책 고유 식별자를 지정합니다.
* Effect
  * Allow | Deny
  * 허용 또는 불허 여부를 나타냅니다.
* Action
  * String | Array<String>
  * 하나 혹은 여러 개의 Action을 지정할 수 있습니다.
  * [AWS 서비스에 사용되는 작업, 리소스 및 조건 키](https://docs.aws.amazon.com/ko_kr/service-authorization/latest/reference/reference_policies_actions-resources-contextkeys.html)
    * 각 서비스별로 고유의 서비스 접두사(예: DynamoDB는 dynamodb)가 있습니다.
    * 위 설명서에서 각 서비스를 선택해서 들어가면 XX에서 정의한 작업이라는 단락에 작업 목록이 있습니다.
    * Action은 (서비스 접두사):(작업) 형식으로 작성합니다.
      * 예: dynamodb:DeleteItem, s3:GetObject 등
* Resource
  * Statement의 주제가 되는 리소스를 한정합니다.
  * 리소스는 [ARN](https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/reference-arns.html) 형식을 사용합니다. ARN은 AWS 리소스를 고유하게 식별하는데 사용합니다. 
  * 서비스에 따라서 서비스에 속한 하부 리소스 단위까지 한정할 수도 있고 리소스를 특정할 수 없는 일부 서비스에서는 Resource를 비워두는 대신 `*`를 사용합니다.
    * 예) DynamoDB의 테이블 수준까지 한정
* Principal
  * object (Principal)
  * 리소스 기반 정책에서, 보안 주체를 지정합니다.
* Sid(Statement ID)
  * string
  * 각 요소에 Unique ID를 붙이고 싶을 때 사용합니다.

<br>

### Policy 종류
* 연결 대상이 어딘지에 따른 분류
  * 자격증명 기반 정책(Id-Based Policy)
    * 자격 증명 주체는 User, Group, Role
  * 리소스 기반 정책(Resource-Based Policy)
  * Role의 신뢰 관계
* 누가 만들었느냐에 따른 분류
  * 내가 만든 경우 => 고객 관리 정책
  * AWS 빌트인 => AWS 관리 정책, 직무 기반 정책
* Role에 인라인으로 선언되었다면 => 인라인 정책

중요한 유형은 자격 증명 기반 정책과 리소스 기반 정책입니다.  
정책이라는 것은 사실 “(어떤 Condition을 만족할 때,) 어떤 Resource에 대해서 어떤 Action을 할 수 있게 허용/불허(Effect)한다.”라는 의미를 담은 것입니다.  

하지만 위에는 주어가 빠져있습니다. 이것을 보안 주체(Principal)이라고 합니다.  

완전한 정책이 되려면, “(어떤 Principal이) (어떤 Condition을 만족할 때,) 어떤 Resource에 대해서 어떤 Action을 할 수 있게 허용/불허(Effect)한다.” 이렇게 되어야 합니다.  

<br>

#### 자격증명 기반 정책(Id-Based Policy)
자격증명-기반 정책(Id-Based Policy)으로 사용할 때는, 그 정책에 연결된 보안 주체(AWS 계정, IAM 사용자, 그룹, 역할 혹은 연합 인증된 사용자, Cognito User Identity 등이 될 수 있음)가 암묵적인 Principal이 됩니다. 그래서 자주 보던 정책의 Statement에서 대개 Principal 속성이 생략되어 있습니다.  

> IAM 메뉴의 정책 리스트에 나오는 정책들은 모두 자격증명 기반 정책입니다.

<br>

#### 리소스 기반 정책(Resource-Based Policy)
리소스-기반 정책은 AWS의 서비스 중에서 일부 서비스에서 사용됩니다. 자격증명 기반 정책은 ‘이 정책과 연결된 보안주체는 어떤 대상에 대해 이러이러한 권한이 부여된다’는 의미였습니다. 그런데 반대로 객체(대상)를 중심으로 생각을 해보면 어떨까요? (마치 능동태를 수동태로 바꾸듯이)

> ‘이 대상에 대해 이러이러한 권한이 누구누구누구에게 부여된다’

리소스 기반 정책을 사용하는 대표적인 서비스는 S3입니다. S3의 버킷의 권한 메뉴에 들어가면 버킷 정책을 편집할 수 있습니다.
```json
{
    "Version": "2008-10-17",
    "Id": "PolicyForCloudFrontPrivateContent",
    "Statement": [
        {
            "Sid": "1",
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::cloudfront:user/CloudFront Origin Access Identity E36E6CTWWW86E5"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::hello.world.net/*"
        }
    ]
}
```
<br>

> arn:aws:(서비스 prefix):(aws 리전이름):(aws계정):(리소스 한정자)

arn은 위와 같이 읽습니다. 하나씩 해석해보겠습니다.

<br>

> "arn:aws:iam::cloudfront:user/CloudFront Origin Access Identity E36E6CTWWW86E5"

* 서비스 prefix: iam
* aws 리전 이름 : cloudfront는 리전이 없어서 생략
* aws 계정 : cloudfront
* 리소스 한정자 : user
* 서비스 식별자가 iam이고 aws 계정이 cloudfront인데 특별한 예약 규칙으로 cloudfront를 위해서 별도의 보안주체를 관리하는 리소스 그룹 user이 있따는 것을 말해줍니다.

<br>

> "arn:aws:s3:::hello.world.net/*"

* 서비스 prefix : s3
* aws 리전 : 생략
* aws 계정 : 생략
* 리소스 한정자 : hello.world.net 버킷에 있는 모든 오브젝트

<br>

정리하면 다음과 같습니다.

> S3 버킷 hello.world.net에 업로드 된 모든 오브젝트의 읽을 권한(s3:GetObject)를 CloudFront Origin Access Identity E36E6CTWWW86E5에 부여한다.


<br>

결과적으로 리소스 기반 정책은 principal 속성이 들어간다는 것이 자격 증명 기반 정책과의 차이입니다. 그리고 자격증명 기반 정책을 AWS IAM에서 직접 관리하지만, 리소스 기반 정책은 보통 S3예시처럼 해당 서비스의 버킷 정책 편집 탭에서 리소스에서 자체적으로 관리합니다.


<br>

##
