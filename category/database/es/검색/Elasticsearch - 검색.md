## 단건 문서 API

### 색인
```json
PUT [인덱스 이름]/_doc/[_id값]  // 가장 기본
POST [인덱스 이름]/_doc
PUT [인덱스 이름]/_create/[_id값]
POST [인덱스 이름]/_create/[_id값]
```
PUT의 경우 요청 본문에 담아 보낸 JSOPN 문서를 지정된 인덱스와 _id값으로 색인을 시도합니다. 만약 같은 _id값을 가진 문서가 있다면 덮어씌웁니다. 반면에 POST 메서드는 _id값을 지정하지 않고 색인을 요청할 경우에 사용합니다. 이 경우에는 랜덤한 _id값이 지정됩니다. _create이 경로에 들어가는 API는 항상 새로운 문서 생성만을 허용하고 덮어씌우면서 색인하는 것은 금지합니다.

### 조회
```json
GET [인덱스 이름]/_doc/[_id값]
```
조회 API는 문서 단건을 조회합니다. 이는 검색과는 다르게 refresh됮 않은 상태에서도 변경된 내용을 확인할 수 있습니다. 애초에 고유한 식별자를 지정해서 단건 문서를 조회하는 것은 역색인을 사용할 필요가 없기 때문에 translog에서 읽어옵니다.

### 필드 필터링

```json
GET [인덱스]/_doc/[_id값]?_source_includes=p*,views
GET [인덱스]/_doc/[_id값]?_source_includes=p*,views&_source_excludes=public
```
조회 API 사용 시 _source_includes와 _source_excludes 옵션을 사용하면 결과에 원하는 필드만 필터링해서 포함시킬 수 있습니다. 전자는 포함시키는 것이고 후자는 제외시키는 것입니다.

### 업데이트 API
```json
POST [인덱스 이름]/_update/[_id값]
{
  "doc": {
    [업데이트할 내용]
  }
}
```
루씬의 세그먼트는 불변이기 때문에 업데이트 시 기존 문서를 수정하는 것이 아니라 기존 문서의 내용을 조회한 뒤 부분 업데이트될 내용을 합쳐 새 문서를 만들어 색인하는 형태로 진행됩니다. 그리고 현재 문서와 동일한 내용이라 업데이트할 것이 없다면 검색 결과 result 필드에 noop이라는 응답이 나옵니다. 


```json
POST [인덱스 이름]/_update/[_id값]
{
  "doc": {
    [업데이트할 내용]
  },
  "doc_as_upsert": true
}
```
업데이트 API는 기존 문서를 조회한 뒤 업데이트 시키기 때문에 기존 문서가 없다면 실패합니다. 기존 문서가 없을 때에는 새로 문서를 추가하는 upsert가 필요하다면 옵션을 켜야합니다.

### 삭제 API
```json
DELETE [인덱스 이름] // 인덱스 전체 삭제
DELETE [인덱스 이름]/_doc/[_id값] // 특정 문서 삭제
```

## 복수 문서 API
### bulk API
bulk API는 여러 색인, 업데이트, 삭제 작업을 한 번의 요청에 담아서 보내는 API입니다.

```json
POST [인덱스 이름]/_bulk
```
```json
POST _bulk
{"index": {"_index": "bulk_test","_id":"1"}}
{"field": "value1"}
{"delete": {"_index": "bulk_test","_id":"2"}}
{"create": {"_index": "bulk_test","_id":"1"}}
{"field": "value1"}
{"update": {"_id":"1","_index": "bulk_test"}}
{"field": "value1"}
```
index, create 요청은 색인 요청인데 index는 색인, 덮어쓰기가 가능하고 create는 새 문서를 생성하는 것만 허용하고 덮어쓰지는 않습니다. _bulk API의 경우 인덱스 이름을 명시하지 않은 경우, 요청 body에 index이름을 따라가고,  인덱스 이름을 명시하고 요청 body에 index이름을 명시하지 않는다면 인덱스 이름을 따라갑니다. 응답은 각 요청을 수행하고 난 결과를 모아 하나의 응답으로 돌아옵니다. 응답으로 나온 결과의 순서가 bulk API의 실행 순서를 의미합니다.

bulk API의 기술된 작업이 반드시 그 순서대로 수행된다고 보장되진 않습니다. 조정 역할을 하는 노드가 요청을 수신하면 각 요청의 내용을 보고 적절한 주 샤드로 요청을 넘겨주고 여러 개의 주 샤드에 넘어간 각 요청은 각자 독립적으로 수행되기 때문에 요청 간 순서는 보장되지 않습니다. 하지만 완전히 동일한 인덱스, _id, 라우팅 조합을 가진 요청은 반드시 동일한 주 샤드로 넘어갑니다. 이 경우에는 한 bulk API 내에서 이 조합이 같은 요청에 대해서는 bulk API가 기술된 순서대로 동작합니다.


### multi get API
```json
GET _mget
GET [인덱스 이름]/_mget

// ex
GET bulk_test/_mget
{
  "ids": ["1", "2"]
}
```

## 검색 API
검색에 대해 설명하기 앞서, 엘라스틱서치에서 와일드카드 검색을 사용하지 않는 것이 권장됩니다. 사용해야 한다면 반드시 매핑 설정과 데이터 양상을 파악한 뒤 그 여파를 정확히 판단할 수 있는 상태에서만 제한적으로 사용해야 합니다. 그 중에서도 *ello, ?ello 같이 와일드카드 문자가 앞에 오는 쿼리는 더욱 주의해야 합니다. 인덱스가 들고 있는 모든 term을 가지고 검색을 돌려서 확인하기 때문입니다. 

### 검색 대상 지정과 쿼리 DSL 검색
```json
GET [인덱스 이름]/_search
POST [인덱스 이름]/_search
GET _search
POST _search

// ex
GET my_test*,mapping_test/_search
```
GET, POST 둘다 동작은 동일하며 인덱스 이름을 지정하지 않으면 전체 인덱스에 대해 검색합니다. 인덱스 이름을 지정할 때는 와일드카드 문자를 사용할 수 있고 콤마로 구분하여 검색 대상을 여럿 지정하는 것도 가능합니다.

검색 쿼리를 지정하는 방법은 요청 본문에 엘라스틱서치 전용 쿼리 DSL을 명시하는 방법과 요청 주소줄에 q라는 매개변수를 넣고 루씬 쿼리 문자열을 지정해 검색하는 방법이 있습니다. 쿼리가 매우 단순한 경우가 아니라면 후자는 거의 사용하지 않습니다.
```json
GET my_test/_search
{
  "query": {
    "match": {
      "title": "hello"
    }
  }
}
```

### match_all
```json
GET [인덱스 이름]/_search
{
  "query": {
    "match_all": { }
  }
}
```
match_all 쿼리는 모든 문서를 매치하는 쿼리입니다. query 부분을 비워두면 기본값으로 지정되는 쿼리입니다.

### match
```json
GET [인덱스 이름]/_search
{
  "query": {
    "match": {
      "title": "hello world"
    }
  }
}
```
match는 지정한 필드의 내용과 일치하는 문서를 찾습니다. 필드가 text 타입이라면 애널라이저로 분석됩니다. 위에서는 hello, world 두 토큰으로 분석되어 2개의 텀을 찾아 매치되는 문서를 반환합니다. 이때 match 쿼리는 OR 조건으로 동작합니다. 만약 and 조건으로 변경하고 싶다면 operator를 추가해야 합니다.

```json
GET [인덱스 이름]/_search
{
  "query": {
    "match": {
      "title": {
        "query": "hello world",
        "operator": "and"
      }
    }
  }
}
```
### term
```json
GET [인덱스 이름]/_search
{
  "query": {
    "term": {
      "fieldName": {
        "value": "VALUE"
      }
    }
  }
}
```
term 쿼리는 지정한 필드 값이 정확히 일치하는 문서를 찾습니다. 대상 필드에 노멀라이저가 지정돼 있다면 질의어도 노멀라이저가 처리됩니다. term 쿼리는 대상이 text타입인 경우, 질의어는 노멀라이저 처리를 거치지만 필드의 값은 애널라이저로 분석한 뒤 생성된 역색인을 이용합니다. 즉, 필드 값이 애널라이저로 분석되어 역색인 된 토큰들과 노멀라이저 처리된 질의어가 완전히 일치하는 경우에만 검색에 걸립니다.

### terms
```json
GET [인덱스 이름]/_search
{
  "query": {
    "terms": {
      "fieldName": ["hello", "world"]
    }
  }
}
```
term 쿼리와 유사하지만 질의어를 여러 개 지정할 수 있습니다. 하나 이상의 질의어가 일치하면 검색 결과에 포함됩니다.

### range 쿼리
```json
GET [인덱스 이름]/_search
{
  "query": {
    "range": {
      "fieldName": {
        "gte": 100,
        "lt": 200
      }
    }
  }
}
```
지정한 필드의 값이 특정 범위 내에 있는 문서를 찾습니다. 범위는 gt, lt, gte, lte를 사용할 수 있습니다. 엘라스틱서치는 문자열 필드를 대상으로 한 range 쿼리는 부하가 큰 쿼리로 분류하므로 주의해야 합니다.

```json
GET [인덱스 이름]/_search
{
  "query": {
    "range": {
      "dateField": {
        "gte": "2023-02-15T11:11:11.000Z||+36h/d",
        "lte": "now-3h/d"
      }
    }
  }
}
```

+ now : 현재 시각
+ || : 날짜 시간 문자열의 마지막에 붙이고 이 뒤에 붙는 문자열은 시간 계산식으로 파싱됩니다.
+ +와 - : 지정된 시간만큼 더하거나 빼는 연산을 수행합니다.
+ / : 버림을 수행합니다. 예를 들어 /d는 날짜 단위 이하의 시간을 버림합니다.

**날짜 시간 단위**

기호|단위
---|---
y|연도
M|월
w|주
d|날짜
h|시간
H|시간
m|분
s|초

### prefix
```json
GET [인덱스 이름]/_search
{
  "query": {
    "prefix": {
      "fieldName": {
        "value": "hello"
      }
    }
  }
}
```
필드값이 지정한 질의어로 시작하는 문서를 찾는 쿼리로 무거운 쿼리로 분류됩니다. 와일드카드 쿼리만큼은 아니지만 단발성 쿼리 정도는 감수할 만한 성능이 나옵니다. 하지만 일반적인 서비스성 쿼리로는 적절하지 못합니다. 만약 서비스용으로 사용해야 한다면 매핑시에 index_prefixes를 설정에 넣는 방법을 고려해야 합니다.

```json
PUT prefix
{
  "mappings": {
    "properties": {
      "fieldName": {
        "type": "text",
        "index_prefixes":{
          "min_chars": 3,
          "max_chars": 5
        }
      }
    }
  }
}
```
매핑 시에 index_prefixes를 지정하면 색인할 때, min, max_chars 사이의 prefix를 미리 별도 색인을 합니다.

### exists
```json
PUT [인덱스 이름]/_search
{
  "query": {
    "exists": {
      "field": "fieldName"
    }
  }
}
```
지정한 필드를 포함한 문서가 있는지 검색합니다.

### bool 쿼리
```json
PUT [인덱스 이름]/_search
{
  "query": {
    "bool": {
      "must": [
        {"term": {
          "title": {
            "value": "hello"
          }
        }},
        {"term": {
          "body": {
            "value": "world"
          }
        }}
      ],
      "must_not": [
        {"term": {
          "message": {
            "value": "backtony"
          }
        }}
      ],
      "filter": [
        {"term": {
          "message": "terrapy"
        }}
      ],
      "should": [
        {"match": {
          "field2": "world"
        }}
      ],
      "minimum_should_match": 1
    }
  }
}
```
여러 쿼리를 조합하여 검색합니다. must, must_not, filter, should 4가지 종류의 조건절에 다른 쿼리를 조합하여 사용합니다. must 조건절과 filter 조건절에 들어간 하위 쿼리는 모두 AND 조건으로 만족해야 최종 검색 결과에 포함됩니다. must_not 조건절에 들어간 쿼리를 만족하는 문서는 제외되며 should 조건절에 들어간 쿼리는 minimum_should_match에 지정한 개수 이상의 하위 쿼리를 만족하는 문서가 결과에 포함됩니다. minimum_should_match의 기본값은 1이며 이 값이 1이라면 should 조건절은 or 조건으로 검색하는 것과 같습니다.  

bool 쿼리는 여러 쿼리를 조합하게 되는데 어떤 쿼리가 먼저 수행되는지에 대한 규칙은 없습니다. 엘라스틱서치가 쿼리를 받으면 내부적으로 쿼리를 재작성하기 때문입니다. 

### 쿼리 문맥과 필터 문맥
참과 거짓으로 따지는 검색 과정을 필터 문맥이라고 하고, 문서가 주어진 검색 조건을 얼마나 더 만족하는지 유사도 점수를 매기는 검색 과정을 쿼리 문맥이라고 합니다. 

비고|쿼리 문맥|필터 문맥
---|---|---
질의 개념|얼마나 잘 매치되는지|조건을 만족하는지
점수|계산 O|계산 X
성능|상대적 느림|상대적 빠름
캐시|쿼리 캐시 X|쿼리 캐시 O
종류| bool의 must, should 또는 match, term 등|bool의 filter, must_not 또는 exists, range, constant_score 등


### 라우팅
```json
GET [인덱스 이름]/_search?routing=[라우팅]
{
  "query": {
    // ...
  }
}
```
검색, 색인, 조회 API 모두 제대로된 라우팅을 지정하는 것이 좋습니다. 라우팅을 지정하지 않으면 전체 샤드에 검색 요청이 들어갑니다. 반면 라우팅을 지정하면 정확히 한 샤드에만 검색 요청이 들어가므로 성능상 이득이 매우 큽니다. 물론 비즈니스 특성상 전체 샤드를 대상으로 검색을 수행해야한다면 라우팅을 지정하지 않아야 합니다.

### 정렬
```json
GET [인덱스 이름]/_search
{
  "query": {
    // ...
  },
  "sort": [
    {"field1":  { "order": "desc"}},
    {"field2":  { "order": "asc"}},
    "field3"
  ]
}
```
sort를 지정해서 검색 결과를 정렬할 수 있습니다. 정렬 대상 필드를 여럿 지정할 수 있고 fields 같이 이름만 명시하면 내림차순으로 정렬됩니다. 정렬 대상 필드의 타입으로 숫자, date, boolean, keyword 타입은 가능하지만 text 타입은 대상으로 지정할 수 없습니다. 필드 이름 외에도 _score(유사도)나 _doc(문서 번호 순서)를 지정할 수 있습니다. sort 옵션을 지정하지 않으면 기본은 _score 내림차순이고, 정렬 옵션에 _score가 포함되지 않으면 유사도를 계산하지 않습니다. 만약 어떤 순서로 정렬되어도 상관 없는 경우라면 _doc 단독 정렬을 사용하면 좋습니다.

### 페이지네이션

#### from과 size
```json
PUT [인덱스 이름]/_search
{
  "from": 10,
  "size":5,
  "query": {
    // ..
  }
}
```
size는 몇 개의 문서를 반환할 지, from은 몇 번째 문서부터 결과를 반환할지를 의미합니다. 위의 경우 유사도 점수를 내림차순 정렬된 문서들 중, 11번째부터 15번째 까지 5개의 문서가 반환됩니다. from의 기본값은 0, size는 10입니다.  

from과 size는 제한적으로 사용해야 하며 본격적인 페이지네이션에는 사용할 수 없습니다. 만약 다음 페이지의 결과를 가져오기 위해 from 15, size 5로 지정해 검색을 요청했다고 한다면 내부적으로는 20개의 문서를 수집하는 검색을 수행한 뒤 마지막의 결과 일부를 반환하는 방식으로 동작합니다. 이런 동작과정 때문에 2가지 문제가 있습니다.

1. from의 값이 올라갈수록 매우 무거운 검색을 수행하게 된다.
2. 이전 페이지를 검색할 때의 상태와 페이지를 넘기고 다음 검색을 수행할 때의 인덱스 상태가 동일하지 않다.

두 검색 요청 사이에 새로운 문서가 색인되거나 삭제될 수도 있기 때문에 특정 시점의 데이터를 중복이나 누락 없이 엄밀하게 페이지네이션을 제공해야한다면 from과 size는 사용하지 말아야 합니다. 첫 번째 문제 때문에 엘라스틱서치는 from과 size를 조합해 검색할 수 있는 최대 윈도우 크기를 from과 size의 합을 1만개로 제한하고 있습니다. 인덱스를 늘릴 수 는 있지만 이 방법은 권하지 않고 있으며 scroll이나 search_after 방법을 사용해야 합니다.


#### scroll
scroll은 검색 조건에 매핑되는 전체 문서를 모두 순회해야 할 때 적합한 방법입니다. 스크롤을 순회하는 동안에는 최초 검색 시의 문맥(search context)이 유지됩니다. 중복이나 누락도 발생하지 않습니다. scroll 매개변수로 검색 문맥을 유지할 시간을 지정해서 검색할 수 있습니다.

```json
// 첫 검색
PUT [인덱스 이름]/_search?scroll=1m
{
  "size": 1000,
  "query": {
    // ..
  }
}

// response
{
  "_scroll_id": "abcde"
}

// 두번째 검색
GET _search/scroll
{
  "scroll_id": "abcde"        
  "scroll": "1m"
}
```
첫 번째 검색의 response로 scroll_id가 반환되고 이후부터는 scroll_id로 검색하면 더 이상 문서가 반환되지 않을 때까지, scroll 검색을 반복할 수 있습니다. scroll 검색을 한 번 수행할 때마다 검색 문맥이 연장됩니다. 즉, scroll 매개변수에 지정한 검색 문맥의 유지 시간은 배치와 배치 사이를 유지할 정도의 시간으로 지정하면 됩니다. 

scroll은 검색 문맥을 보존한 뒤 전체 문서를 순회하는 동작 특성상 검색 결과의 정렬 여부가 상관없는 작업에 사용하는 경우가 많습니다. 이 경우에는 _doc로 정렬을 지정하는 것이 좋습니다. 이렇게 지정하면 유사도 점수를 계산하지 않으며 정렬을 위한 별도의 자원도 사용하지 않습니다. 이를 통해 scroll 성능을 끌어올릴 수 있습니다.

```json
// 첫 검색
PUT [인덱스 이름]/_search?scroll=1m
{
  "size": 1000,
  "query": {
    // ..
  },
  "sort": [
    "_doc"    
  ]
}
```
scroll은 서비스에서 지속적 호출을 의도하고 만들어진 기능이 아닙니다. 주로 대량의 데이터를 다른 스토리지로 이전하거나 덤프하는 용도로 만들어졌기 때문에 서비스에서 사용자가 지속적으로 호출하기 위한 용도는 아닙니다.

#### search_after
서비스에서 사용자가 검색 결과를 페이지네이션으로 제공하는 용도의 경우라면 search_after이 가장 적합합니다. search_after에는 sort를 지정해야 합니다. 이때 동일한 정렬 값이 등장할 수 없도록 최소한 1개 이상의 동점 제거(tiebreaker: 동일 스코어를 가진 문서들 사이에 어떤 문서를 먼저 반환할지)용 필드를 지정해야 합니다. 

```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 20,
  "query": {
    "term": {
      "currency": "EUR"
    }
  },
  "sort": [
    {
      "order_date": "desc",
      "order_id": "asc"
    }
  ]
}

// response
{
  // ...
    "hits" : [
      {
        "sort" : [
          1709411990000,
          "591924"
        ]
      }
    ]
  }
}
```
첫 번째 검색이 끝나면 결과의 가장 마지막 문서에 표시된 sort 기준값을 가져와 search_after 부분에 넣어 그 다음 검색을 요청합니다.

```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 20,
  "query": {
    "term": {
      "currency": "EUR"
    }
  },
  "search_after": [1709411990000, "591924"],
  "sort": [
    {
      "order_date": "desc",
      "order_id": "asc"
    }
  ]
}
```

동점 제거용 필드(tiebreaker)는 스코어가 동일한 경우 어떤 문서를 먼저 반환할 것인지를 정하는 것이기 때문에 문서를 고유하게 특정할 수 있는 값이 들어가야 합니다. 그러나 _id값을 동점 제거용 기준 필드로 사용하는 것은 좋지 않습니다. _id 필드는 doc_values가 꺼져있기 때문에 이를 기준으로 하는 정렬은 많은 메모리를 사용하게 됩니다. 따라서 _id 필드값과 동일한 값을 별도의 필드에 저장해 뒀다가 동점 제거용으로 사용하는 것이 좋습니다.

그러나 동점 제거용 필드를 제대로 지정했다 하더라도 인덱스 상태가 변하는 도중이라면 페이지네이션 과정에서 누락되는 문서가 발생하는 등 일관적이지 않은 변동 사항이 발생할 수 있습니다. search_after를 사용할 때 인덱스 상태를 특정 시점으로 고정하려면 point in time API를 조합해서 사용해야 합니다.

#### point in time API
point in time API는 검색 대상의 상태를 고정할 때 사용합니다. keep_alive 매개변수에 상태를 유지할 시간을 지정합니다. 해당 기능은 7.10 버전 이후부터 사용할 수 있으며, oss버전에서는 지원되지 않습니다.

```json
POST /kibana_sample_data_ecommerce/_pit?keep_alive=1m

// response
{
  "id": "abcd"
}
```
위에서 얻은 id값을 search_after에서 사용할 수 있습니다.
```json
GET _search
{
  "size": 20,
  "query": {
    // ..
  },
  "pit": {
    "id": "abcd",
    "keep_alive": "1m"
  },
  "sort": [
    {
      "order_date": "desc"
    }
  ]
}
```
pit 부분에 얻어온 pit id를 지정해서 사용하고, 검색 대상이 될 인덱스는 지정하지 않습니다. pit를 지정하는 것 자체가 검색 대상을 지정하는 것이기 때문입니다. pit를 지정하면 동점 제거용 필드를 별도로 지정할 필요가 없습니다. 정렬 기준 필드를 하나라도 지정했다면 _shard_doc이라는 동점 제거용 필드에 대한 오름차순 정렬이 맨 마지막에 자동으로 추가됩니다. 다만 정렬 기준을 아예 지정하지 않으면 정렬 기준으로 _shard_doc이 추가되지 않습니다. 따라서 search_after에서 조합해서 사용하고 싶은 경우 정렬 기준 필드를 최소한 하나는 지정해야 합니다. 

```json
// 다음 검색
GET _search
{
  "size": 20,
  "query": {
    // ..
  },
  "pit": {
    "id": "abcd",
    "keep_alive": "1m"
  },
  "search_after": [1709411990000, "591924"],
  "sort": [
    {
      "order_date": "desc"
    }
  ]
}
```
