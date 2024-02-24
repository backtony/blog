## order by 처리(using filesort)
정렬을 처리하는 방법은 인덱스를 이용하는 방법과 쿼리가 실행될 때 Filesort라는 별도의 처리를 이용하는 방법으로 나눌 수 있다.

* 인덱스 이용
    * 장점
        * insert, update, delete 쿼리가 실행될 때 이미 인덱스가 정렬돼 있어서 순서대로 읽기만 하면 되므로 매우 빠름
    * 단점
        * insert, update, delete 작업 시 부가적인 인덱스 추가/삭제 작업이 필요하므로 느리다.
        * 인덱스 때문에 추가 디스크 공간이 필요하고 늘어날수록 innoDB의 버퍼 풀을 위한 메모리가 많이 필요하다.
* filesort 이용
    * 장점
        * 인덱스를 생성하지 않아도 되므로 인덱스를 이용할 때의 단점이 장점으로 바뀐다.
        * 정렬해야 할 레코드가 많지 않으면 메모리에서 filesort가 처리되므로 충분히 빠르다.
    * 단점
        * 정렬 작업이 쿼리 실행 시 처리되므로 레코드 대상 건수가 많아질수록 느려진다.


다음과 같은 이유로 모든 경우에 대해서 정렬을 인덱스 튜닝하기는 불가능하다.
* 정렬 기준이 너무 많아서 요건별로 모두 인덱스 생성이 불가능한 경우
* group by의 결과 또는 distinct 같은 처리의 결과를 정렬해야 하는 경우
* union의 결과와 같이 임시 테이블의 결과를 다시 정렬해야하는 경우
* 랜덤하게 결과 레코드를 가져와야하는 경우

인덱스를 잘 활용하는지 아닌지는 explain을 떠보면 extra 컬럼에 using filesort 메시지가 표시되는지 여부로 파악할 수 있다.

쿼리에 order by를 사용하면 반드시 3가지 처리중 한 가지가 사용된다.
정렬 처리 방법|실행 계획의 extra 컬럼 내용
---|---
인덱스를 사용한 정렬|X
조인에서 드라이빙(기준) 테이블만 정렬|using filesort
조인에서 조인 결과를 임시 테이블로 저장 후 정렬|using temporary; using filesort

인덱스를 이용할 수 있다면 별도의 표기가 없고 사용할 수 없다면 where 조건에 일치하는 레코드를 검색해 정렬 버퍼에 저장하면서 정렬을 처리(file sort)한다. mysql 옵티마이저는 정렬 대상 레코드를 최소화하기 위해 다음 2가지 방법 중 선택한다.

* 조인의 드라이빙 테이블(기준)만 정렬한 다음 조인을 수행
* 조인이 끝나고 일치하는 레코드를 모두 가져온 후 정렬을 수행

일반적으로 조인이 수행되면 레코드 건수와 레코드의 크기가 배수로 불어나기 때문에 가능하다면 드라이빙 테이블(기준)만 정렬한 다음 조인을 수행하는 방법이 효율적이다.

### 인덱스를 이용한 정렬
인덱스를 이용한 정렬을 위해서는 반드시 ORDER BY 절에 명시된 컬럼이 드라이빙 테이블(기준 테이블)에 속하고 ORDER BY의 순서대로 생성된 인덱스가 있어야 한다. 또한 WHERE 절에 드라이빙 테이블(기준 테이블)의 컬럼에 대한 조건이 있다면 그 조건과 ORDER BY는 같은 인덱스를 사용할 수 있어야 한다.

```sql
SELECT *
FROM employee e, salaries s
WHERE s.emp_no = e.emp_no
    AND e.emp_no BETWEEN 100 AND 200
ORDER BY e.emp_no
```

### 조인의 드라이빙 테이블(기준 테이블)만 정렬
일반적으로 조인이 되면 데이터가 불어나기 때문에 드라이빙 테이블만 먼저 정렬하고 조인을 실행하는 것이 차선책이 된다. 이 방법으로 정렬이 처리되려면 드라이빙 테이블의 컬럼만으로 ORDER BY 절을 작성해야 한다.

```sql
SELECT *
FROM employee e, salaries s
WHERE s.emp_no = e.emp_no
    AND e.emp_no BETWEEN 100 AND 200
ORDER BY e.last_name
```

where절이 다음 조건을 갖추고 있어서 옵티아미저는 employee 테이블을 드라이빙 테이블로 선택할 것이다.
* where 절의 검색 조건은 employee의 프라이머리 키를 이용해 검색하면 작업을 줄일 수 있다.
* 드리븐 테이블(salaries)의 조인 컬럼인 emp_no 컬럼에 인덱스가 있다.

검색은 인덱스로 처리할 수 있지만 order by 절에 명시된 컬럼이 프라이머리 키와 전혀 연관이 없으므로 인덱스를 이용한 정렬이 불가능하다. 그런데 정렬 컬럼이 드라이빙 테이블에 포함된 컬럼이므로 옵티마이저는 드라이빙 테이블만 검색해서 정렬을 먼저 수행하고 그 결과를 salaries 테이블과 조인한다.

### 임시 테이블을 이용한 정렬
조인의 드라이빙 테이블 사용을 제외한 2개 이상의 테이블 조인에서는 항상 조인 결과를 임시 테이블에 저장하고 그 결과를 다시 정렬하는 과정을 거친다. 이 방법은 3가지 방법중에 가장 느린 방법이다.
```sql
SELECT *
FROM employee e, salaries s
WHERE s.emp_no = e.emp_no
    AND e.emp_no BETWEEN 100 AND 200
ORDER BY s.salary
```
order by 정렬 컬럼이 드리븐 테이블(salaries)에 있는 컬럼이다. 즉, 정렬이 수행되기 전에 salaries 테이블을 읽어야 하므로 조인된 데이터를 가지고 정렬할 수밖에 없다. 따라서 explain을 떠보면 filesort, using temporary가 표기된다.


## GROUP BY 처리
GROUP BY 절이 있는 쿼리에서는 Having 절을 사용할 수 있는데 이는 GROUP BY 결과에 대해 필터링할 수 있는 역할을 수행한다. `GROUP BY에 사용된 조건은 인덱스를 사용해서 처리될 수 없으므로 Having 절을 튜닝하려고 인덱스를 생성하거나 다른 방법을 고민할 필요는 없다.`

GROUP BY 절도 인덱스를 사용하는 경우와 그렇지 못한 경우로 나뉜다. 인덱스를 이용할 때는 인덱스를 차례대로 읽는 인덱스 스캔 방법과 인덱스를 건너뛰면서 읽는 루스 인덱스 스캔이라는 방법으로 나뉜다. 그리고 인덱스를 사용하지 못하는 쿼리에서 GROUP BY 작업은 임시 테이블을 사용한다.

### 인덱스 스캔을 이용하는 GROUP BY(타이트 인덱스 스캔)
ORDER BY의 경우와 마찬가지로 조인의 드라이빙 테이블에 속한 컬럼만 이용해서 그루핑할 때 GROUP BY 컬럼으로 이미 인덱스가 있다면 그 인덱스를 차례로 읽으면서 그루핑 작업을 수행하고 그 결과로 조인을 처리한다. GROUP BY가 인덱스를 사용해서 처리된다 하더라도 그룹함수(aggregation function) 등의 그룹값을 처리해서 임시 테이블이 필요할 때도 있다. GROUP BY가 인덱스를 통해 처리되는 쿼리는 이미 정렬된 인덱스를 읽는 것이므로 쿼리 실행 시점에 추가적인 정렬 작업이나 내부 임시 테이블은 필요하지 않다. 이러한 그루핑 방식을 사용하는 쿼리의 실행 계획에서는 Extra 컬럼에 별도로 GROUP BY 관련 코멘트인 (using index for group-by)나 임시 테이블 사용 또는 정렬 관련 코멘트(using temporary, using filesort)가 표시되지 않는다.

### 루스 인덱스 스캔을 이용하는 GROUP BY
루스 인덱스 스캔 방식은 인덱스의 레코드를 건너뛰면서 필요한 부분만 읽어서 가져오는 것을 의미하는데, 옵티마이저가 루스 인덱스 스캔을 사용할 때는 실행 계획의 Extra 컬럼에 `Using index for group-by 코멘트가 표시된다.`

```sql
select emp_no
from salaries
where from_date = '1984-03-01'
group by emp_no
```
salaries 테이블의 인덱스는 (emp_no, from_date) 로 생성되어있으므로 where 조건은 인덱스 레인지 스캔 접근 방식으로는 이용할 수 없다. 하지만 explain을 떠보면 인덱스 레인지 스캔을 이용했으며 extra 컬럼 메시지에 인덱스 사용이 표기된다.(using where; using index for group-by)

이 방식은 단일 테이블 group by에서만 사용할 수 있고 prefix index(컬럼값의 앞쪽 일부만으로 생성된 인덱스)는 사용할 수 없다.

### 임시 테이블을 사용하는 GROUP BY
이는 기준 컬럼이 드라이빙에 있는 드리븐에 있든 관계없이 인덱스를 전혀 사용하지 못할때 사용된다.
```sql
select e.last_name, avg(s.salary)
from employees e, salaries s
where s.emp_no = e.emp_no
group by e.last_name
```
실행 계획을 떠보면 using temporary가 표기된다. 테이블 풀 스캔이 아니라 인덱스를 전혀 사용하지 못하기 때문이다. 그런데 using filesort는 표기되지 않고 useing temporary만 표기된다. 8.0 이전버전에는 group by가 사용된 쿼리는 그루핑되는 컬럼을 기준으로 묵시적인 정렬까지 함께 수행됐다. 그래서 이전에는 group by는 있지만 order by 절이 없는 쿼리에 대해서는 기본적으로 그루핑 컬럼인 last_name에 대해서 정렬이 수행된 상태로 결과값을 반환했다. 하지만 8.0부터는 묵시적인 정렬은 실행되지 않고 반환된다.

이러한 이유 때문에 8.0 이전에는 group by후 정렬이 필요하지 않은 경우, `order by null` 사용이 권장되었다.

mysql 8.0에서는 group by가 필요한 경우 내부적으로 group by 절의 컬럼으로 구성된 유니크 인덱스를 가진 임시 테이블을 만들어서 중복 제거와 집합 함수 연산을 수행한다.
```sql
create temporary table ... (
    last_name varchar(16),
    salary int,
    unique index ux_lastname (last_name)
)
```

## Distinct 처리
distinct 처리는 인덱스를 사용하지 못할 때는 항상 임시테이블이 필요하다. 하지만 실행 계획에서는 using temporary가 출력되지 않는다.

### select distinct
```sql
select distinct first_name, last_name from employees;
select distinct (first_name), last_name from employees;
```
distinct키워드는 뒤에 () 괄호는 실행시 제거된다. 즉, distinct는 first_name만 유니크한게 아니고 (first_name, last_name) 조합 전체가 유니크한 레코드를 가져온다.

이에 대한 예외는 집합 함수와 함께 사용되는 경우다.

### 집합 함수와 함께 사용된 distinct
count, min, max 같은 집합 함수 내에서 distinct 키워드가 사용될 수 있는데 일반적으로 select distinct와는 다른 형태로 해석된다. select 쿼리에서 distinct는 조회 컬럼 모든 조합이 유니크한 것들만 가져오지만 집합 함수 내에서는 사용된 distinct의 함수 인자로 전달된 컬럼 값이 유니크한 것들만 가져온다.
```sql
select count(distinct s.salary)
from employees e, salaries s
where e.emp_no=s.emp_no
and e.emp_no between 100 and 200
```
쿼리는 내부적으로 count를 처리하기 위해 임시테이블을 사용하지만 실행 계획에는 표기되지 않는다. 위 쿼리는 조인 결과에서 salary 컬럼의 값만 저장하기 위한 임시테이블을 만들어서 사용한다. 임시 테이블의 salary 컬럼에는 유니크 인덱스가 생성되기 때문에 레코드 건수가 많아진다면 상당히 느려질 수 있다.

```sql
select count(distinct s.salary)
    count(distinct e.last_name)
from employees e, salaries s
where e.emp_no=s.emp_no
and e.emp_no between 100 and 200
```
만약 위와 같이 count 쿼리를 추가하면 임시 테이블이 2개가 생긴다.

## 임시 테이블이 필요한 쿼리
다음과 같은 패턴은 별도의 데이터 가공이 필요하여 내부 임시 테이블이 생성되는 케이스다. 물론 이 밖에도 인덱스를 사용하지 못할 때는 내부 임시테이블을 생성해야할 때가 많다.

* order by와 group by에 명시된 컬럼이 다른 쿼리
* order by나 group by에 명시된 컬럼이 조인의 순서상 첫 번째 테이블이 아닌 쿼리
* distinct와 order by가 동시에 쿼리에 존재하는 경우
* distinct가 인덱스로 처리되지 못하는 쿼리
* union, union distinct가 사용된 쿼리(select_type 컬럼이 union result인 경우)
* 쿼리의 실행 계획에서 select_type이 DERIVED인 쿼리


## SELECT 절 실행 순서
```sql
select s.emp_no, COUNT(DISTINCT e.first_name) AS cnt
FROM salaries s
    INNER JOIN employees on e.emp_no=s.emp_no
WHERE s.emp_no IN (10001, 10002)
GROUP BY s.emp_no
HAVING ABG(s.salary) > 1000
ORDER BY AVG(s.salary)
LIMIT 10;
```
1. FROM 및 조인
2. WHERE 절 
3. GROUP BY
4. DISTINCT
5. HAVING
6. ORDER BY
7. LIMIT

순으로 적용된다.

위 순서와 다르게 예외적으로 다음과 같은 순서로 적용되는 경우가 있다.
1. 드라이빙(주) 테이블 WHERE 절 적용
2. ORDER BY 절
3. 드리븐(대상) 테이블 조인 실행
4. LIMIT

이와 같은 형태는 주로 GROUP BY 절 없이 ORDER BY만 사용된 쿼리에서 사용될 수 있는 순서다.

위와 같은 두가지 순서가 거의 모든 쿼리에서 적용된다. 이에 벗어나는 쿼리 순서가 있다면 서브쿼리로 작성된 인라인 뷰를 사용해야 한다.

## WHERE, GROUP BY, ORDER BY 인덱스 사용
### 인덱스 사용 기본 규칙
인덱스를 사용하려면 기본적으로 인덱스된 컬럼 값 자체를 변환하지 않고 그대로 사용한다는 조건을 만족해야 한다. 즉, WHERE 절에서 사용할 떄, 가공(곱셈 같은 연산)을 하면 제대로 적용되지 않는다.

### where 절 인덱스 사용
`where 조건절에 나열된 컬럼 순서는 실제 인덱스의 사용 여부와 무관하다.` where 절에 나열된 컬럼 순서와 인덱스의 컬럼 순서가 다르더라도 옵티마이저가 조건들을 뽑아서 최적화를 수행한다. 8.0이전 버전까지는 하나의 인덱스를 구성하는 각 컬럼의 `정렬 순서`가 혼합되어 사용할 수 없었지만 8.0부터는 인덱스를 구성하는 컬럼별로 오름차순, 내림차순 정렬을 혼합해서 생성할 수 있게 개선되었다.
```sql
ALTER TABLE ... ADD INDEX hello (col_1 ASC, col_2 DESC)
```
여러 방향으로 정렬을 하고 싶다면 위처럼 인덱스를 만들때, ASC, DESC 옵션을 섞어서 인덱스를 만들어주면 된다.

### GROUP BY 절 인덱스 사용
* `WHERE절과 달리 group by 절에 명시된 컬럼이 인덱스 컬럼의 순서와 위치가 같아야 한다.`
* 인덱스를 구성하는 컬럼 중 뒤쪽에 있는 컬럼은 GROUP BY 절에 명시되어있지 않아도 인덱스를 사용할 수 있지만 앞쪽에 있는 컬럼이 GROUP BY 절에 명시되지 않으면 인덱스를 사용할 수 없다.
* WHERE 조건절과 달리 GROUP BY는 인덱스가 하나라도 명시되어있지 않으면 전혀 인덱스를 사용하지 못한다.

### ORDER BY 절 인덱스 사용
GROUP BY 절의 조건과 일치하고 하나더 조건이 있는데 정렬되는 각 컬럼의 오름차순 및 내림차순 옵션이 인덱스와 같거나 정반대인 경우에만 사용할 수 있다. 예를 들면, 인덱스의 모든 컬럼이 오름차순으로 정렬되어있다면 order by절은 모든 컬럼이 오름차순이거나 내림차순일 때만 사용할 수 있다. 그리고 모든 인덱스가 order by에 명시되어야 하는 것은 아니지만 순서는 일치해야 한다.

### WHERE + order by
where절과 order by 절이 같이 사용된 쿼리는 다음 3가지 중 하나로 동작한다.

* WHERE 절과 ORDER BY 절이 동시에 같은 인덱스를 이용
    * where 절의 비교 조건에서 사용하는 컬럼과 order by 절의 정렬 대상 컬럼이 모두 하나의 인덱스를 연속해서 포함돼 있을 때 이 방식으로 인덱스를 사용한다.
* WHERE 절만 인덱스를 이용
    * order by 절은 인덱스를 이용한 정렬이 불가능하며, 인덱스를 통해 검색된 결과 레코드를 별도의 정렬 처리 과정(file sort)을 거쳐 정렬을 수행한다. 주로 WHERE 조건절에 일치하는 레코드 건수가 많지 않을 때 효율적이다.
* ORDER BY 절만 인덱스를 사용
    * ORDER BY 절의 순서대로 인덱스를 읽으면서 레코드 한 건씩 where 조건에 일치하는지 비교하고 일치하지 않을 때는 버리는 형태로 처리한다.

또한 where 절에서 조건으로 사용된 컬럼과 order by 절에 명시된 컬럼 순서가 인덱스 컬럼의 왼쪽부터 일치해야 한다. 중간에 빠지는 부분이 있으면 모두 인덱스를 사용할 수 없다.

### group by + order by
두 절 모두 하나의 인덱스를 사용해서 처리되려면 둘다 명시된 컬럼의 순서와 내용이 모두 같아야 한다. 둘중 하나라도 인덱스를 이용할 수 없을때는 둘다 인덱스를 사용할 수 없다.

### where + order by + group by
1. where 절이 인덱스를 탈 수 있는가?
2. group by 절이 인덱스를 사용할 수 있는가?
3. group by 절과 order by 절이 동시에 인덱스를 사용할 수 있는가?

위 세가지 조건을 만족하면 인덱스를 태울 수 있다.

## limit
```sql
select * from .. limit 0 10;
```
limit이 없다면 풀 테이블 스캔이 실행된다. 위는 limit 조건이 있으므로 풀 테이블 스캔을 실행하면서 엔진은 10개의 레코드를 읽어들이는 순간 작업을 멈춘다. 이렇게 정렬, 그루핑, distinct가 없으면 쿼리가 상당히 빨리 끝난다.

```sql
selecgt * from .. group by .. limit 0 10;
```
group by가 있으므로 group by가 종료되야 limit을 처리할 수 있다. 따라서 limit이 있더라도 작업을 크게 줄여주진 못한다.

```sql
select distinct .. from .. limit 0 10;
```
엔진은 풀 테이블 스캔을 하면서 레코드를 읽음과 동시에 distinct 중복 제거 작업(임시 테이블 사용)을 진행한다. 이 작업을 처리하다가 유니크한 레코드가 limit 건수만큼 채워지면 쿼리를 종료한다.

```sql
select * from .. where .. order by .. limit 0 10;
```
where 조건에 맞는 레코드를 읽고 정렬을 수행한다. 정렬을 수행하면서 10건이 완성되면 쿼리를 종료한다. 레코드를 전부 읽고 정렬을 해야하므로 작업량이 크게 줄지는 않는다.

## count
레코드 건수를 반환하는 것으로 count(*) 를 사용할 수 있다. 여기서 *는 레코드 자체를 의미하므로 프라이머리 컬럼을 넣거나 1을 넣는 것이랑 똑같이 동작한다. 보통 쿼리 작성할 때 기존 쿼리에서 프로젝션 부분만 count로 바꿔서 사용하는 경우가 있는데 이러면 불필요한 order by 같은 것이 들어가는 경우가 발생할 수 있다. 그래서 **8.0부터는 count 쿼리에서 order by는 무시하도록 옵티마이저가 처리한다.** 하지만 이를 알고 있더라도 굳이 쿼리의 복잡도가 높아보이도록 작성할 필요는 없기에 count 쿼리에 불필요한 절을 제거할 수 있도록 하자.

## join
### join의 순서와 인덱스
조인에서 드라이빙 테이블을 읽을 때는 인덱스 탐색 작업을 단 한번만 수행하고 이후부터는 스캔만 하면 된다. 하지만 드리븐 테이블에서는 인덱스 탐색 작업과 스캔 작업을 드라이빙 테이블을 읽은 레코드 건수만큼 반복한다. 그래서 옵티마이저는 드라이빙 테이블보다 드리븐 테이블을 최적으로 읽을 수 있게 실행계획을 수립한다.

### outer join 성능과 주의사항
아우터 조인시 실수하는 상황은 아우터 조인되는 테이블에 대한 조건을 where 조건 절에 사용하는 것이다.
```sql
select * 
from employee e
    left join dept_manager mgr on mgr.emp_no = e.emp_no
    where mgr.dept_no='001';
```
on 절에 조인 조건을 명시하고 where 조건절에 outer 조인 되는 드리븐 테이블의 컬럼을 조건으로 사용했다. 하지만 옵티아미저는 아우터 조인으로 사용된 드리븐 테이블의 컬럼을 where 조건으로 사용한 것을 보고 해당 쿼리를 inner join으로 바꿔버린다. 따라서 정상적으로 아우터 조인이 되게 만들려면 다음과 같이 where절의 조건을 on절로 옮겨야 한다.
```sql
select * 
from employee e
    left join dept_manager mgr on mgr.emp_no = e.emp_no AND mgr.dept_no='001'; 
```

이것의 예외에 해당하는 경우는 안티 조인 효과를 기대하는 단 한가지의 경우가 있다.
```sql
select * 
from employee e
    left join dept_manager mgr on mgr.emp_no = e.emp_no
    where mgr.dept_no IS NULL;
```
위는 NULL인 레코들만 조회한다. 이것이 아우터 드리븐 테이블이 조인에서 where 조건절로 사용할 수 있는 유일한 경우다. 그 외의 경우에는 아우터 드리븐 테이블이 where 조건절로 오면 MySQL 서버는 LEFT JOIN을 inner join으로 변경해버린다.


## 잠금을 사용하는 SELECT
레코드를 select할때는 lock 없이 진행되는데 select가 실행된 후 애플리케이션에서 가공해서 업데이트 할때는 다른 트랜잭션이  그 컬럼 값을 변경하지 못하게 해야 한다. 이럴 때는 읽으면서 레코드에 잠금을 걸어 둘 필요가 있다. 이때 사용하는 옵션이 for share와 for update 절이다. for share는 읽기 잠금이고 for update는 select 쿼리가 읽은 레코드에 대해 쓰기 잠금을 건다. 해당 옵션이 사용되었더라고 일반적인 select 구문은 그대로 대기 없이 데이터를 읽어갈 수 있다. 즉, 해당 옵션이 사용된 쿼리끼리만 잠금이 걸리는 것이다.

## 잠금 테이블 선택
8.0 이전에서는 조인한 테이블 전체에 모두 잠금이 걸렸다. 하지만 조인된 테이블은 참고용이라면 8.0에서는 쿼리에 사용된 테이블 중 특정 테이블만 잠금을 획득하는 옵션으로 `for share(for update) of 테이블 별칭` 이 추가되었다.

