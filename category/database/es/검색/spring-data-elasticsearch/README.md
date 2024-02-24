## docker compose elk 설치

> https://github.com/deviantony/docker-elk

위 깃허브를 clone하고 elsticsearch 디렉토리의 Docker 파일에 analysis-nori 설치를 추가합니다.

```dockerfile
ARG ELASTIC_VERSION

FROM docker.elastic.co/elasticsearch/elasticsearch:${ELASTIC_VERSION}

RUN elasticsearch-plugin install analysis-nori
```

## config 세팅
application.yml에 spring.data.elasticsearch 로도 세팅할 수 있지만 이 경우에는 ConnectionRequestTimeout를 세팅할 수 없으므로 ElasticserachConfig.class 에서 세팅합니다.   
