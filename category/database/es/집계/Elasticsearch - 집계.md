
## 집계
엘라스틱서치의 집계는 검색의 연장선입니다. 집계의 대상을 추려낼 검색 조건을 검색 API에 담은 뒤 집계 조건을 추가해서 호출합니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "term": {
      "currency": {
        "value": "EUR"
      }
    }
  },
  "aggs": {
    "my-agg": {
      "sum": {
        "field": "taxless_total_price"
      }
    }
  }
}
```
검색 요청 본문에 aggs를 추가하고 size는 0으로 지정했습니다. size를 0으로 지정하면 검색에 상위 매칭된 문서가 무엇인지 받아볼 수 없지만 검색 조건에 매치되는 모든 문서는 집계 작업 대상에 사용됩니다. 집계 작업은 검색 대상을 받아보는 용도가 아니기 때문에 size는 0으로 지정하여 점수를 계산하는 과정도 생략할 수 있습니다. 집계 요청의 상세는 aggs 밑에 기술하고 요청 한 번에 여러 집계를 요청할 수도 있기 때문에 결과에서 구분할 수 있도록 집계에 이름을 붙입니다.

### 메트릭 집계

#### avg, max, min, sum
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "term": {
      "currency": {
        "value": "EUR"
      }
    }
  },
  "aggs": {
    "my-agg": {
      "sum": {
        "field": "taxless_total_price"
      }
    }
  }
}
```
sum 자리에 avg, max, min으로 대체하여 똑같이 사용할 수 있습니다.

#### stats
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "term": {
      "currency": {
        "value": "EUR"
      }
    }
  },
  "aggs": {
    "my-agg": {
      "stats": {
        "field": "taxless_total_price"
      }
    }
  }
}
```
stats 집계는 지정한 필드의 평균, 최댓값, 최솟값, 합, 개수를 모두 계산해서 반환합니다.

#### cardinality
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "term": {
      "currency": {
        "value": "EUR"
      }
    }
  },
  "aggs": {
    "my-agg": {
      "cardinality": {
        "field": "customer_id",
        "precision_threshold": 3000
      }
    }
  }
}
```
지정한 필드가 가진 고유한 값의 개수를 계산합니다. precision_threshold 옵션은 정확도를 조절하기 위해 사용합니다. 이 값을 높이면 정확도가 올라가지만 그만큼 메모리를 더 사용합니다. 해당 옵션을 무작정 높일 필요는 없고 precision_threshold가 최종 cardinality보다 높다면 정확도가 충분히 높기 때문에 적당한 값을 지정해 주는 것이 좋습니다. 기본값은 3000이며 최대값은 40000입니다. cardinality가 높고 낮음과 관계없이 메모리 사용량은 precision_threshold에만 영향을 받습니다.

### 버킷 집계

#### range
```json
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-range-agg": {
      "range": {
        "field": "DistanceKilometers",
        "ranges": [
          {
            "to": 5000
          },
          {
            "from": 5000,
            "to": 10000
          },
          {
            "from": 10000
          }
        ]
      },
      "aggs": {
        "my-price-agg":{
          "avg": {
            "field": "AvgTicketPrice"
          }
        }
      }
    }
  }
}

// response
{
  "aggregations" : {
    "my-range-agg" : {
      "buckets" : [
        {
          "key" : "*-5000.0",
          "to" : 5000.0,
          "doc_count" : 4052,
          "my-price-agg" : {
            "value" : 513.3930266305937
          }
        },
        {
          "key" : "5000.0-10000.0",
          "from" : 5000.0,
          "to" : 10000.0,
          "doc_count" : 6042,
          "my-price-agg" : {
            "value" : 677.2621444606182
          }
        },
        {
          "key" : "10000.0-*",
          "from" : 10000.0,
          "doc_count" : 2965,
          "my-price-agg" : {
            "value" : 685.3553124773563
          }
        }
      ]
    }
  }
}
```
range 밑에 aggs를 하나 더 넣어서 하나의 집계에 대한 하위 집계를 추가할 수 있습니다. 하위 집계의 depth가 깊어지면 성능상 심각한 문제가 발생하므로 주의해야 합니다.

#### date_range
range 집계와 유사하나 date 타입 필드를 대상으로 한다는 점에서 from과 to에 간단한 날짜 시간 계산식을 사용할 수 있다는 점에 차이가 있습니다.


#### histogram
```json
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-histo": {
      "histogram": {
        "field": "DistanceKilometers",
        "interval": 1000
      }
    }
  }
}
```
지정한 필드의 값을 기준으로 버킷을 나눈다는 점에서 range 집계와 유사합니다. 다른 점은 버킷 구분의 경계 기준값을 직접 지정하는 것이 아니라 버킷의 간격을 지정해서 경계를 나눕니다. interval을 지정하면 필드의 최솟값과 최댓값을 확인한 후 그 사이를 interval에 지정한 간격을 쪼개서 버킷을 나눕니다. 특별히 지정하지 않으면 0을 기준으로 쪼개기를 시작하고 위치를 조정하고 싶다면 offset을 사용할 수 있습니다.

```json
GET kibana_sample_data_flights/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-histo": {
      "histogram": {
        "field": "DistanceKilometers",
        "interval": 1000,
        "offset": 500
      }
    }
  }
}
```
offset을 500으로 지정하면 [500,1500) 구간부터 시작할 것으로 예상할 수 있지만, interval이 1000이므로 [0,500) 사이의 구간은 [-500, 500) 구간이 추가되어 해당 구간에 포함됩니다. 이외에도 min_doc_count를 지정해서 버킷 내 문서 개수가 일정 이하인 버킷은 결과에서 제외할 수 있습니다.

#### date_histogram
histogram 집계와 유사하지만 대상으로 date 타입 필드를 사용한다는 점이 다릅니다. intervval 대신 calendar_interval이나 fixed_interval을 사용합니다.

#### terms 
```json
GET kibana_sample_data_logs/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-terms": {
      "terms": {
        "field": "host.keyword",
        "size": 10
      }
    }
  }
}
```
지정한 필드에 대해 가장 빈도수가 높은 term 순서대로 버킷을 생성합니다. 버킷을 최대 몇 개까지 생성할 것인지를 size로 지정합니다. terms 집계는 각 샤드에서 size 개수만큼 term를 뽑아서 빈도수를 셉니다. 각 샤드에서 수행된 계산을 한 곳으로 모아 합산한 후 size 개수만큼 버킷을 뽑습니다. 그러므로 size 개수와 각 문서의 분포에 따라 그 결과가 정확하지 않을 수 있습니다. 각 버킷의 doc_count는 물론 하위 집계 결과도 정확하지 않을 수 있고 특히 해당 필드의 고유한 term 개수가 size보다 많다면 상위에 뽑혀야 할 term이 최종 결과에 포함되지 않을 수도 있습니다.

응답 본문에 doc_count_error_upper_bound 필드는 doc_count의 오차 상한선을 나타냅니다. 이 값이 크다면 size를 높이는 것을 고려해야하는데 size를 높이면 정확도는 높아지지만 성능은 하락합니다. sum_other_doc_count 필드는 최종적으로 버킷에 포함되지 않은 문서수를 나타냅니다.

만약 모든 term에 대해서 페이지네이션으로 전부 순회하며 집계를 하려고 한다면 size를 높이는 것보다는 composite 집계를 사용하는 것이 좋습니다.

#### composite 
```json
GET kibana_sample_data_logs/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-compos": {
      "composite": {
        "size": 100,
        "sources": [
          {
            "terms-aggs": {
              "terms": {
                "field": "host.keyword"
              }
            }
          },
          {
            "date-his-agg":{
              "date_histogram": {
                "field": "@timestamp",
                "calendar_interval": "day"
              }
            }
          }
        ]
      }
    }
  }
}
```
sources로 지정한 하위 집계의 버킷 전부를 페이지네이션을 이용해 효율적으로 순회하는 집계입니다. sources에 하위 집계를 여러 개 지정한 뒤 조합된 버킷을 생성할 수 있습니다. composite 아래의 size는 페이지네이션 한 번에 몇 개의 버킷을 반환할 것인가를 지정합니다. sources에는 버킷을 조합하여 순회할 하위 집계를 지정합니다. 모든 종류의 집계를 하위 집계로 지정할 수는 없고 terms, histogram, date_histogram 등 일부 집계만 지정할 수 있습니다.

예를 들어, A 집계의 key가 1, 2이고, B 집계의 key가 a, b, c 라면 둘 조합은 다음과 같이 6개의 버킷으로 나뉘게 됩니다.
```json
A : 1, B : a
A : 1, B : b
A : 1, B : c
A : 2, B : a
A : 2, B : b
A : 2, B : c
```

첫 번째 검색 결과에는 after_key가 포함되서 응답으로 오는데 두 번째 검색부터는 after_key 내용을 추가하면 됩니다.

```json
GET kibana_sample_data_logs/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "my-compos": {
      "composite": {
        "size": 100,
        "sources": [
          //..
        ],
        "after": {
          "terms-aggs" : "cdn.elastic-elastic-elastic.org",
          "date-his-agg" : 1710288000000
        }
      }
    }
  }
}
```

### 파이프라인 집계
파이프라인 집계는 다른 집계 결과를 대상으로 집계 대상으로 합니다. 주로 buckets_path라는 인자를 통해 다른 집계의 결과를 가져오며 이는 상대 경로로 지정합니다. buckets_path는 다음을 구문을 사용합니다.

+ \> : 하위 집계로 이동하는 구분자
+ . : 하위 메트릭으로 이동하는 구분자
+ 집계 이름
+ 메트릭 이름

buckets_path는 하위 경로로 이동할수는 있지만 상위 경로로는 이동할 수 없습니다.

#### cumulative_sum
다른 집계의 값을 누적하여 합산합니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "daily-timestamp": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "day"
      },
      "aggs": {
        "daily-total-quantity-average": {
          "avg": {
            "field": "total_quantity"
          }
        },
        "pipeline-sum":{
          "cumulative_sum": {
            "buckets_path": "daily-total-quantity-average"
          }
        }
      }
    }
  }
}
```
일 단위로 total_quantity의 평균을 구하기 위해 date_histogram으로 버킷을 나눈 뒤 그 하위 집계로 avg를 집계합니다. 그리고 date_histogram의 하위 집계로 cumulative_sum 집계를 추가했습니다. cumulative_sum은 buckets_path에서 누적 합산을 수행할 집계로 daily-total-quantity-average를 지정합니다. 이렇게 하면 cumulative_sum을 수행할 때마다 daily-total-quantity-average를 찾아서 그 합을 누적 합산합니다.

#### max_bucket
```json
GET kibana_sample_data_ecommerce/_search
{
  "size": 0,
  "query": {
    "match_all": {}
  },
  "aggs": {
    "daily-timestamp": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "day"
      },
      "aggs": {
        "daily-total-quantity-average": {
          "avg": {
            "field": "total_quantity"
          }
        }
      }
    },
    "max-total-quantity":{
      "max_bucket": {
        "buckets_path": "daily-timestamp>daily-total-quantity-average"
      }
    }
  }
}
```
다른 집계의 결과를 받아서 그 결과가 가장 큰 버킷의 key와 결과값을 구합니다. 
