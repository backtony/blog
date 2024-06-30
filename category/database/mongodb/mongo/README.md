## mongoDB docker replica 형태로 띄우기

### key file 생성
key file은 replica set에 참여하는 mongod 인스턴스 간의 인증, 클라이언트 접속 시 access control에 사용됩니다.
key file이 없는 상태에서 컨테이너를 실행하려고 하면 에러가 발생합니다.
```shell
# mongodb 키 생성
sudo openssl rand -base64 756 > ~/.ssh/replica-mongodb-test.key

# 권한 설정
sudo chmod 400 ~/.ssh/replica-mongodb-test.key

# 제대로 key가 생성되었는지 확인
cat ~/.ssh/replica-mongodb-test.key
```

### 실행 
```shell
# 실행
docker-compose up -d

# 정상적으로 올라왔는지 확인
docker ps -a
```

### 레플리카셋 초기화
```shell
# container 접속
docker exec -it mongo1 /bin/bash

# root 계정 몽고 쉘 접속
mongosh -u root -p root

# admin 데이터베이스 사용
use admin

# replication 초기화
rs.initiate()

# mongo2 복제세트 추가
rs.add({_id: 1, host: "mongo2:27017"})

# 리플리카 셋 설정 정보 확인
rs.config()

# 리플리카 셋 상태정보 확인
rs.status()
```
