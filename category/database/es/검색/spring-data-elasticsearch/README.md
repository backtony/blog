## docker compose elk 설치

> https://github.com/deviantony/docker-elk

위 깃허브를 clone하고 elsticsearch 디렉토리의 Docker 파일에 analysis-nori 설치를 추가합니다.

```dockerfile
ARG ELASTIC_VERSION

FROM docker.elastic.co/elasticsearch/elasticsearch:${ELASTIC_VERSION}

RUN elasticsearch-plugin install analysis-nori
```

## 인덱스 템플릿
```json
PUT _index_template/article
{
  "index_patterns": ["article-v*"],
  "priority": 1,
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 1,
      "index": {
        "max_ngram_diff": 5
      },
      "analysis" : {
        "normalizer": {
          "lower_case_normalizer": {
            "type":"custom",
            "filter":["lowercase","asciifolding"]
          }
        },
        "analyzer" : {
          "korean" : {
            "type" : "custom",
            "tokenizer" : "korean_nori_tokenizer"
          },
          "ngram_analyzer" : {
            "tokenizer" : "ngram_tokenizer"
          }
        },
        "tokenizer" : {
          "korean_nori_tokenizer" : {
            "type" : "nori_tokenizer"
          },
          "ngram_tokenizer" : {
            "type" : "ngram",
            "min_gram" : "2",
            "max_gram" : "5"
          }
        }
      }
    },
    "mappings": {
      // 라우팅을 true로 하게 되면 모든 검색이 전부 라우팅이 명시해야 함.
      // "_routing": {
      //   "required": true
      // }, 
      "properties": {
        "title": {
          "type": "text",
          "analyzer" : "ngram_analyzer",
          "fields": {
            "keyword": {
              "type": "keyword"
            }
          }
        },
        "body": {
          "type": "text",
          "analyzer" : "korean"
        },
        "attachment": {
          "type": "object",
          "properties": {
            "name": {
              "type": "keyword"
            },
            "path": {
              "type": "keyword"
            }
          }
        },
        "authors": {
          "type": "nested",
          "properties": {
            "name": {
              "type": "keyword", 
              "normalizer": "lower_case_normalizer"
            },
            "age": {
              "type": "integer"
            }
          }
        },
        "registeredDate": {
          "type": "date",
          "format" : "yyyy-MM-dd HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ss.SSSZ||epoch_millis"
        },
        "lastModifiedDate": {
          "type": "date",
          "format" : "yyyy-MM-dd HH:mm:ss.SSSZ||yyyy-MM-dd'T'HH:mm:ss.SSSZ||epoch_millis"
        }
      }
    }
  }
}
```
```json
PUT /article-v1


POST _aliases
{
  "actions": [
    {
      "add": {
        "index": "article-v1",
        "alias": "article"
      }
    }
  ]
}
```

## config 세팅
application.yml에 spring.data.elasticsearch 로도 세팅할 수 있지만 이 경우에는 ConnectionRequestTimeout를 세팅할 수 없으므로 ElasticserachConfig.class 에서 세팅합니다.   

## 라우팅
도메인에 @Routing 애노테이션을 사용하면 ElasticsearchRepository를 사용하는 경우 save 시, 라우팅이 자동으로 세팅되지만 findXX같은 경우에는 라우팅이 자동으로 명시되지 않으므로 별도로 구현해서 사용해야 합니다. `ArticleCustomRepositoryImpl 참고` 
