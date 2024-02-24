## 설정하기

### build.gradle.kts
```groovy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
    kotlin("kapt") version "1.7.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

//noArg {
//    annotation("jakarta.persistence.Embeddable")
//    annotation("jakarta.persistence.MappedSuperclass")
//    annotation("jakarta.persistence.Entity")
//}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
//    implementation("org.hibernate.common:hibernate-commons-annotations:6.0.6.Final")

    implementation("mysql:mysql-connector-java:8.0.31")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

```

### application.yml
```yml
spring:
  datasource:
    hikari:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://localhost:3306/kotlin_jpa?serverTimezone=UTC&characterEncoding=UTF-8
      username: root
      password: root
      maximum-pool-size: 5
      minimum-idle: 5
      connection-timeout: 5000

  jpa:
    database: mysql
    generate-ddl: true
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    hibernate:
      ddl-auto: create-drop
    open-in-view: false

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace
```

### QuerydslConfig 
```kotlin
@Configuration
class QuerydslConfig {
    @PersistenceContext
    private val em: EntityManager? = null

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(em)
    }
}
```

## 기본 문법
### 기본 Q-Type 활용
```java
// 애노테이션 생략
@RequiredArgsConstructor
public class QuerydslBasicTest {
    Private final JPAQueryFactory queryFactory;
    
    // Querydsl을 사용하기 위해서는 JPAQueryFactory가 필요하다.
    // jpaQueryFactory를 만들때 생성자 파라미터로 EntityManager을 넣어줘야한다.
    public QuerydslBasicTest(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Test
    void startquerydsl() throws Exception{
        // compileQuerydsl로 만들어진 QXXX을 사용하여 query를 작성한다.
        // querydsl에서 사용하는 Member을 꺼내온다.
        // 결국 쿼리에서는 m을 기준으로 사용하게 된다.
        // QMember m = QMember.member;

        // 하지만 이것 또한 static import로 줄일 수 있다.       
        // 그냥 쿼리에서 전부 QMember.member하되 
        // Qmember을 static import하게되면 결론적으로 member만으로 사용 가능

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        
        // 같은 테이블을 조인해야하는경우 별칭이 같으면 안되므로
        // 다른 별칭을 사용해야함
        // member는 그대로 사용하고
        // QMember memberSub = new QMember("memberSub");
        // 따로 하나 만들어서 사용
    }
}
```
+ compileQuerydsl로 만들어진 Q엔티티 을 사용하여 쿼리 작성합니다.
+ Q엔티티는 static import하면 따로 선언 없이 사용할 수 있습니다.
+ 파라미터 바인딩을 여러가지로 처리 가능합니다.(eq...)
+ 같은 테이블을 조인해야 하는 경우 다른 별칭을 주어 사용합니다.
+ select와 from이 같은 파라미터를 가지면 selectFrom으로 합칠 수 있습니다.

### 검색 조건 쿼리
Querydsl은 JPQL이 제공하는 모든 검색 조건을 제공합니다.
```java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() //이름이 is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색
```

<br>

```java
// and, or도 가능
Member findMember = queryFactory
 .selectFrom(member)
 .where(member.username.eq("member1")
 .and(member.age.eq(10)))
 .fetchOne();

// and -> 쉼표 처리
queryFactory
 .selectFrom(member)
 .where(member.username.eq("member1"),
        member.age.eq(10))
 .fetch();
```
where 조건을 엮어줄 때 and()와 or을 사용할 수 있습니다. and의 경우 쉼표(,)도 and로 인식하기 때문에 더 깔끔하게 가져갈 수 있습니다.

### 결과 조회
+ fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
+ fetchOne() : 단 건 조회
    - 결과가 없으면 : null
    - 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
+ fetchFirst() : 첫 건만 조회, limit(1).fetchOne() 와 결과 동일
+ fetchResults, fetchCount는 deprecated되었습니다.

```kotlin
@Repository
interface FileRepository : JpaRepository<File, Int>, FileRepositoryCustom

class FileRepositoryCustomImpl(
    private val query: JPAQueryFactory
) : QuerydslRepositorySupport(File::class.java), FileRepositoryCustom {
    override fun findWithLinkHistoryByNo(no: Int): List<File> {

        return query
            .select(file)
            .from(file)
            .leftJoin(fileLinkHistory).on(file.no.eq(fileLinkHistory.file.no))
            .fetch()
    }
}
```

### 정렬
```java
List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
```
+ desc() : 내림차순
+ asc() : 오름차순
+ nullsLast() : null을 제일 마지막으로
+ nullsFirst() : null을 제일 처음으로

### 페이징
#### page
```java
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        // 검색 쿼리
        List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 카운트 쿼리 조립
        // 쿼리만 만들고 fetch같은것 사용하지 않음 
        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                    .leftJoin(member.team, team)
                .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
                );
        return PageableExecutionUtils.getPage(content, pageable,()->countQuery.fetchCount();
}
```
count 쿼리와 paging 쿼리를 별도로 작성해서 사용합니다.

#### slice
Slice 기법이란 일반적인 페이징 방식이 아닌 스크롤을 밑으로 내려가면서 데이터를 불러오는 방식입니다. Slice는 최종 페이지 수를 알 필요가 없으므로 count 쿼리가 필요 없습니다. JPA에서는 Page 대신 Slice로 반환하면 알아서 처리해주지만 QueryDSL에서는 직접 구현해야합니다. 

1. N개의 데이터가 필요하다면 N+1 개의 데이터를 가져옵니다.
2. 결과 값의 개수 > N 이라면 다음 페이지가 존재한다는 뜻입니다.
3. 결과 값의 개수가 > N 라면 추가적으로 가져온 +1 데이터를 빼고 결과 리스트를 반환합니다.

__RepositorySliceHelper__  
Slice 관련 로직을 여러곳에서 사용하기 위해 클래스로 하나 만들어서 사용합니다.

```java
public class RepositorySliceHelper {

    public static <T> Slice<T> toSlice(List<T> contents, Pageable pageable) {

        boolean hasNext = isContentSizeGreaterThanPageSize(contents, pageable);
        return new SliceImpl<>(hasNext ? subListLastContent(contents, pageable) : contents, pageable, hasNext);
    }

    // 다음 페이지 있는지 확인
    private static <T> boolean isContentSizeGreaterThanPageSize(List<T> content, Pageable pageable) {
        return pageable.isPaged() && content.size() > pageable.getPageSize();
    }

    // 데이터 1개 빼고 반환
    private static <T> List<T> subListLastContent(List<T> content, Pageable pageable) {
        return content.subList(0, pageable.getPageSize());
    }
}
```
```java
public Slice<NotificationDto> findNotificationByUsername(String username, Pageable pageable) {

        List<OrderSpecifier> ORDERS = getAllOrderSpecifiers(pageable);

        List<NotificationDto> results = query
                .select(new QNotificationDto(
                        notification.title,
                        notification.message,
                        notification.checked,
                        notification.notificationType,
                        notification.uuid,
                        notification.TeamId
                ))
                .from(notification)
                .where(notification.member.username.eq(username))
                .orderBy(ORDERS.stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return RepositorySliceHelper.toSlice(results, pageable);
    }
```
limit에서 +1로 데이터를 하나 더 가져오고 RepositorySliceHelper를 활용합니다.

### 집계
JPQL이 제공하는 모든 집함 함수를 제공합니다.
```java
@Test
public void aggregation() throws Exception {
    List<Tuple> result = queryFactory
            .select(member.count(),
                    member.age.sum(),
                    member.age.avg(),
                    member.age.max(),
                    member.age.min())
            .from(member)
            .fetch();
    
    Tuple tuple = result.get(0); 
    
    // get의 파라미터로 조회한 그대로를 넣으면 그에 대한 값이 나온다.
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
}
```
Tuple은 Querydsl에서 제공하는 Tuple로 조회하는 것이 여러 개의 타입이 있을 때 사용합니다.

<br>

### 그룹
```java
List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team) 
                // on으로 member.team_id = team.team_id 로 들어갑니다.
                .groupBy(team.name)
                .fetch();
```
그루핑은 join의 파라미터로 엔티티를 넣어주면 on절로 id값들을 묶어줍니다. .having도 가능합니다.  

### 기본 조인
```java
 List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .on(member.name.eq("member"))
                .fetch();

// 나가는 쿼리문
select
        member0_.member_id as member_i1_1_,
        member0_.age as age2_1_,
        member0_.team_id as team_id4_1_,
        member0_.username as username3_1_ 
    from
        member member0_ 
    inner join
        team team1_ 
            on member0_.team_id=team1_.team_id 
    where
        team1_.name=?


Member findMember = queryFactory
        .selectFrom(member)
        .join(member.team, team).fetchJoin() // fetchJoin
        .where(member.username.eq("member1"))
        .fetchOne();
```
+ join 파라미터의 id값끼리 on절로 묶입니다.


### 벌크 연산
```java
long count = queryFactory
            .update(member) // delete 도 가능
            // 수정할 필드, 수정
            .set(member.age, member.age.add(1))
            .execute(); // 다른 쿼리와 다르게 execute 사용
```
벌크연산은 영속성 컨텍스트를 무시하고 DB에 날리므로 항상 실행 이후에는 영속성 컨텍스트를 비워줘야 합니다. 스프링 데이터 JPA에서는 옵션으로 clearAutomatically을 사용하면 비워줄 수 있습니다. Querydsl에는 옵션이 없으므로 em.clear() 로 비워줘야 합니다.  

## 성능 개선
### where 다중 파라미터 사용
where문의 경우 쉼표를 사용할 경우 null을 무시합니다. 예를 들면 where(null, member.~~~ ) 이면 null은 무시되고 member.~~ 만 조건으로 들어가게 됩니다. 이것을 활용하면 간단하고 재활용 가능한 코드로 만들 수 있습니다.

```java
@Test
void 동적쿼리() throws Exception{

private List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
            .selectFrom(member)
            // where 파라미터를 함수로 구성
            .where(usernameEq(usernameCond),ageEq(ageCond))
}

private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond != null ? member.username.eq(usernameCond) : null;
}

private BooleanExpression ageEq(Integer ageCond) {
    return ageCond != null ? member.age.eq(ageCond) : null;
}
```


## 성능 개선
### exist 메서드 개선
기본적으로 JPA에서 제공하는 exists는 조건에 해당하는 row 1개만 찾으면 바로 쿼리를 종료하기 때문에 전체를 찾아보지 않아 성능상 문제가 없습니다. 복잡하게 되면 메소드명으로만 쿼리를 표현하기 어렵기 때문에 보통 @Query를 사용하지만 JPQL의 경우 select의 exists를 지원하지 않습니다. 따라서 count쿼리를 사용해야 하는데 이는 총 몇 건인지 확인을 위해 전체를 봐야하기 때문에 성능이 나쁠 수 밖에 없습니다. 이를 개선하기 위해서 Querydsl의 selectOne과 fetchFirst(= limit 1)을 사용해서 직접 exists 쿼리를 구현해서 개선해야 합니다.
```java
public Boolean exist(Long bookId){
    Integer fetchOne = queryFactory
        .selectOne()
        .from(book)
        .where(book.id.eq(bookId))
        .fetchFirst(); // 한건만 찾으면 바로 쿼리 종료(limit 1)
    
    return fetchOne != null;
}
```
조회결과가 없으면 null이 반환되기 때문에 null로 체크해야 합니다.

<br>

### Cross Join 회피
```java
public List<Customer> crossJoin(){
    return queryFactory
        .selectFrom(customer)
        .where(customer.customerNo.gt(customer.shop.shopNo))
        .fetch();
}

// 쿼리 결과
select
    ....
from
    customer customer_cross // cross 가 cross 조인을 의미함
join
    shop shop1_
where
    ....
```
where 문에서 customer.shop 코드로 인해 묵시적 join으로 Cross Join이 발생합니다. 일부의 DB는 이에 대해 어느정도 최적화가 지원되나 최적화 할수 있음에도 굳이 DB가 해주길 기다릴 필요는 없습니다.
```java
@Query("SELECT c FROM Customer c WHERE c.customerNo > c.shop.shopNo")
List<Customer> crossJoin();

// 쿼리 결과
select
    ....
from
    customer customer_cross // cross 가 cross 조인을 의미함
join
    shop shop1_
where
    ....
```
이는 Hibernate 이슈라서 Spring Data JPA도 동일하게 발생합니다.

```java
public List<Customer> notCrossJoin(){
    return queryFactory
        .selectFrom(customer)
        .innerJoin(customer.shop, shop)
        .where(customer.customerNo.gt(shop.shopNo))
        .fetch();
}
```
위와 같이 명시적으로 조인을 지정해줘서 해결합니다.

### Group By 최적화
MySQL에서 Group By를 실행하면 별도의 Order by이 쿼리에 포함되어 있지 않음에도 Filesort(정렬 작업이 쿼리 실행시 처리되는)가 필수적으로 발생합니다. 인덱스에 있는 컬럼들로 Group by를 한다면 이미 인덱스로 인해 컬럼들이 정렬된 상태이기 때문에 큰 문제가 되지 않으나 굳이 정렬이 필요 없는 Group by에서 정렬을 다시 할 필요는 없기 때문에 이 문제를 해결해야 하는 것이 좋습니다.
```sql
select 1
from ad_offset
group by customer_no
order by null asc;
```
MySQL에서는 order by null을 사용하면 Filesort가 제거되는 기능을 제공하지만 이는 QueryDSL에서는 지원되지 않습니다.  
따라서 이를 직접 구현해야 합니다.
```java
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.NullExpression;

public class OrderByNull extends OrderSpecifier {
    public static final OrderByNull DEFAULT = new OrderByNull();

    private OrderByNull() {
        super(Order.ASC, NullExpression.DEFAULT, NullHandling.Default);
    }
}

// 실제 사용
...
.groupBy(...)
.orderBy(OrderByNull.DEFAULT)
.fetch();
```
null을 그냥 넣게 되면 Querydsl의 정렬을 담당하는 OrderSpecifier 에서 제대로 처리하지 못합니다. Querydsl에서는 공식적으로 null에 대해 NullExpression.DEFAULT 클래스로 사용하길 권장하니 이를 활용합니다. __단, 페이징일 경우, order by null을 사용하지 못하므로 페이징이 아닌 경우에만 사용해야 합니다.__  

> 참고로 정렬이 필요하더라도, 조회 결과가 100건 이하라면, 애플리케이션에서 정렬해야합니다. 일반적인 자원의 입장에서 DB보다는 WAS의 자원이 더 저렴합니다. DB는 3~4대를 사용하더라도 WAS는 수십대를 유지하는 경우가 빈번합니다. 따라서 정렬이란 자원이 필요할 경우 WAS가 DB보다는 여유롭기 때문에 WAS에서 처리하는 것이 좋습니다.  


<br>

## 페이징 개선

### No Offset 으로 구조 변경하기
기존에 사용하는 페이징 쿼리는 일반적으로 아래와 같습니다.
```sql
SELECT *
FROM items
WHERE 조건문
ORDER BY id DESC
OFFSET 페이지번호
LIMIT 페이지사이즈
```
이와 같은 페이징 쿼리가 뒤로 갈수록 느린 이유는 __앞에서 읽었던 행을 다시 읽어야 하기 때문__ 입니다. 예를 들어 offset이 10000이고 limit이 20이라면 결과적으로 10000개부터 20개를 읽어야하니 10020개를 읽고 10000개를 버리는 행위와 같습니다.

<br>

No Offset 방식은 __조회 시작 부분을 인덱스로 빠르게 찾아 매번 첫 페이지만 읽도록 하는 방식__ 입니다.
```java
public List<BookPaginationDto> paginationLegacy(String name, int pageNo, int pageSize) {
    return queryFactory
            .select(Projections.fields(BookPaginationDto.class,
                    book.id.as("bookId"),
                    book.name,
                    book.bookNo))
            .from(book)
            .where(
                    book.name.like(name + "%") // like는 뒤에 %가 있을때만 인덱스가 적용됩니다.
            )
            .orderBy(book.id.desc()) // 최신순으로
            .limit(pageSize) // 지정된 사이즈만큼
            .offset(pageNo * pageSize) // 지정된 페이지 위치에서 
            .fetch(); // 조회
}
```
기존 코드는 위와 같이 offset + limit 까지 읽어와서 offset을 버리고 반환하는 형식 입니다.  
<br>

```java
public List<BookPaginationDto> paginationNoOffset(Long bookId, String name, int pageSize) {

    return queryFactory
            .select(Projections.fields(BookPaginationDto.class,
                    book.id.as("bookId"),
                    book.name,
                    book.bookNo))
            .from(book)
            .where(
                    ltBookId(bookId),
                    book.name.like(name + "%")
            )
            .orderBy(book.id.desc())
            .limit(pageSize)
            .fetch();
}

private BooleanExpression ltBookId(Long bookId) {
    if (bookId == null) {
        return null; // BooleanExpression 자리에 null이 반환되면 조건문에서 자동으로 제거된다
    }

    return book.id.lt(bookId);
}
```
위 코드가 No Offset 방식으로 변경한 코드입니다. 함수에 들어오는 인자에 Id값이 존재합니다. 클라이언트 단에서 현재 갖고있는 id값의 마지막 값을 보내주면 id값을 조건에 넣고 limit으로 원하는 만큼 땡겨오는 방식입니다. 이렇게 작성하면 offset 만큼의 데이터를 읽을 필요가 없게 됩니다. 또한, 클러스터 인덱스인 Id값을 조건문으로 시작했기 때문에 빠르게 조회할 수 있습니다.


### 커버링 인덱스
쿼리를 충족시키는데 필요한 모든 컬럼을 갖고 있는 인덱스로 select / where / order by /group by 등에서 사용되는 모든 컬럼이 인덱스에 포함된 상태를 의미합니다. select 절에서 *를 이용하여 단순히 조회할 경우(where 조건문에 Non Clustered Key(보조 인덱스)를 사용한 경우) Non Clusterd Key에 있는 Clusted Key를 이용해 다시 실제 데이터 접근을 하여 데이터를 가져오게 됩니다.[참고](https://jojoldu.tistory.com/476) 결과적으로 1차적으로 보조 인덱스에 대해 검색하고 2차적으로 cluster index에 대해 검색하게 되는 것입니다. 커버링 인덱스를 사용할 경우 1차적인 검색만으로 끝낼 수 있게 됩니다.

<br>

Book 테이블을 만들고 예시를 들어보겠습니다.
```sql
create table book(
	id bigint not null auto_increment,
    book_no bigint not null,
    name varchar(255) not null,
    type varchar(255),
    primary key(id),
    key idx_name(name)
);

select id, book_no, book_type, name
from book
where name like '200%'
order by id desc
limit 10 offset 10000;
```
위의 select 문에서는 book_no와 book_type이 인덱스가 아니기 때문에 커버링 인덱스가 될 수 없습니다. 결국에는 pk값으로 2차적인 접근을 한다는 뜻인데, 그렇다면 pk값을 커버링 인덱스로 빠르게 가져오고 해당 pk값을 조건문으로 넣으면 pk값에 대한 조회로 빠르게 가져올 수 있을 것입니다. 따라서 Cluster Key(PK)를 커버링 인덱스로 빠르게 조회하고, 조회된 Key로 Select 컬럼들을 후속조회 하는 방식을 사용해야 합니다.
```java
public List<BookPaginationDto> paginationCoveringIndex(String name, int pageNo, int pageSize) {
        // 1) 커버링 인덱스로 대상 조회
        List<Long> ids = queryFactory
                .select(book.id)
                .from(book)
                .where(book.name.like(name + "%"))
                .orderBy(book.id.desc())
                .limit(pageSize)
                .offset(pageNo * pageSize)
                .fetch();

        // 1-1) 대상이 없을 경우 추가 쿼리 수행 할 필요 없이 바로 반환
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        // 2)
        return queryFactory
                .select(Projections.fields(BookPaginationDto.class,
                        book.id.as("bookId"),
                        book.name,
                        book.bookNo,
                        book.bookType))
                .from(book)
                .where(book.id.in(ids))
                .orderBy(book.id.desc())
                .fetch(); 
}
```

커버링 인덱스 방식은 일반적인 페이징 방식에서는 대부분 적용할 수 있지만 몇 가지 단점이 있습니다.
* 너무 많은 인덱스가 필요하다.
  * 결국 쿼리의 모든 항목이 인덱스에 포함되어야하기 때문에 느린 쿼리가 발생할때마다 인덱스가 신규 생성될 수도 있다.
* 인덱스 크기가 너무 커진다.
  * 인덱스도 결국 데이터이기 때문에 너무 많은 항목이 들어가면 성능 상 이슈가 발생할 수 밖에 없는데, where절에 필요한 컬럼외에도 order by, group by, having 등에 들어가는 컬럼들까지 인덱스에 들어가게 되면 인덱스 크기가 너무 비대해진다.
* 데이터 양이 많아지고, 페이지 번호가 뒤로 갈수록 NoOffset에 비해 느리다.
  * 시작 지점을 PK로 지정하고 조회하는 NoOffset 방식에 비해서 성능 차이가 있음 
  * 테이블 사이즈가 계속 커지면 No Offset 방식에 비해서는 성능 차이가 발생



