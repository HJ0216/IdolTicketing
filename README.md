# 데이터 베이스 최적화를 통한 대용량 트래픽 처리
**📺강의:** ***[아이돌 티켓팅 접속자 대기열 시스템](https://fastcampus.co.kr/dev_online_traffic_data)***

## 학습 목표
다량의 요청이 짧은 시간 내에 유입되었을 때, 대기열을 통해 관리하여 장애를 회피하는 방안 습득

## 1. Redis
* Redis
  * 비정형 데이터를 저장하고 관리하는 오픈 소스 데이터베이스 관리 시스템(DBMS)
  * 특징
    * In-Memory 데이터 구조를 가진 저장소
      * 컴퓨터의 주기억장치인 RAM에 데이터를 올려서 사용하는 방법
      * 메모리 내부에서 처리하여, 데이터를 저장/조회할 때 하드디스크를 오고가는 과정을 거치지 않아도 되어 속도가 빠름
      * 서버의 메모리 용량을 초과하는 데이터를 처리할 경우, RAM의 특성인 휘발성에 따라 데이터가 유실될 수 있음
        * 데이터 유실을 방지하기 위해 지속성(persistence) 기능도 제공: 스냅샷(snapshot)이나 AOF(Append-Only File) 방식으로 데이터를 디스크에 저장
    * 데이터에 유효 기간을 설정할 수 있음
      * 메모리에 데이터를 저장하므로 저장 공간이 한정적 → 데이터의 유효 기간을 설정하는 것을 권장
    * 키-밸류(Key-Value) 형태의 구조로 밸류의 자료 구조에 따라 여러 기능을 사용할 수 있음
      * String: 문자열 데이터를 저장 및 조회할 수 있는 기본 자료 구조, 단순 증감 연산 가능
      * Bitmap: 비트 연산을 사용할 수 있는 자료 구조
      * List: 리스트 아이템은 링크드 리스트(Linked List) 형태로 서로 연결되어 있음
      * Hash: 해시 필드와 밸류로 구성, 해시 데이터는 레디스 키와 매핑
      * Set: 순서가 없고, 중복을 허용하지 않는 자료 구조
      * Sorted Set(ZSet): Set과 비슷한 집합 데이터, 정렬 기능을 제공
      * Hyperloglog: 비트 패턴을 분석하여 추정 값을 계산
          예: 특정 상품의 조회수를 1만 239회라고 정확하게 계산할 때는 시스템 부하가 발생 → 추정 값을 계산하는데 최적화되어 1만 회 같은 근사 값을 조회
      * Stream: 이벤트가 발생한 순서대로 데이터를 저장하고 관리하는 구조(데이터 로그와 이벤트 처리에 최적화), 스트림 키 이름과 값, 필드를 사용할 수 있는 자료 구조 형태
  * 활용 사례
    * 캐싱: 데이터베이스 조회나 복잡한 연산 결과를 캐싱하여 성능 향상
    * 메시지 큐(Message Queue): Pub/Sub(발행/구독) 모델을 활용한 실시간 메시징 시스템(데이터를 줄을 세워 순서대로 전달)
    * 세션 관리: 유저 세션 데이터 저장
    * 분산 락: 분산 시스템에서 데이터 동시성을 제어하는 락 관리
    * 로그 및 실시간 분석: Stream과 같은 자료 구조를 활용하여 실시간 데이터 처리


### 1-1. Cache
* Cache
  * 자주 사용하는 데이터나 값을 미리 복사해 놓는 임시 장소
  * Caching Strategy  
  ![caching-strategy](https://github.com/user-attachments/assets/b3e7d9a9-dc6a-47a8-9270-cceea685d58e)
    * Spring에서 Redis를 활용한 Caching Strategy 종류
      * RedisConfig - userRedisTemplate: 객체별로 Redis Template 작성
      * RedisConfig - objectRedisTemplate: 다형성을 활용하여 Redis Template 공유
      * @RedisHash: 객체를 Redis의 Hash 데이터 구조로 저장
        * CrudRepository / RedisRepository를 상속하는 Repository 이용 필수
      * @Cacheable + CacheConfig
        * 캐시에 데이터가 없을 경우 → 캐시에 데이터 추가
        * 캐시에 데이터가 있을 경우 → 캐시의 데이터 반환 
        * @CacheEvict: 캐시된 데이터를 삭제하는 데 사용
        * @CachePut: 캐시된 데이터를 갱신할 때 사용


### 1-2. Monitoring
* Redis 성능을 측정할 주요 지표(메모리 사용량, **히트율**, 처리 속도 등)를 모니터링
* Prometheus
![prometheus_example](https://github.com/user-attachments/assets/1a51cf20-5a30-4b9c-9a2a-00e88928fe7e)
  * 이벤트 모니터링 및 알림에 사용되는 무료 소프트웨어 애플리케이션
  * 기본적으로 Pull 방식(서버에 클라이언트가 떠 있으면 서버가 주기적으로 클라이언트에 접속해서 데이터를 가져오는 방식)을 사용하여 구축된 **시계열 데이터베이스**에 **메트릭**을 기록
    * 다른 대부분의 모니터링 도구가 Push 방식(각 서버에 클라이언트를 설치하고 이 클라이언트가 메트릭 데이터를 수집해서 서버로 보내면 서버가 모니터링 상태를 보여주는 방식)을 사용
* Grafana
![grafana_example](https://github.com/user-attachments/assets/9654e1ed-1aaf-438e-ae53-dfa1648d5472)
  * 시계열 매트릭, 로그 데이터 등을 시각화 하는데 최적화된 대시보드를 제공해주는 오픈소스 툴킷
  * Prometheus, Elasticsearch, MySQL 등 다양한 데이터 소스와 연동하여 사용자 정의 대시보드를 통해 시각화하는데 주로 사용

##### 히트율: 클라이언트가 데이터를 요청했을 때, 캐시에 존재하는 비율로 캐시 히트율이 높으면 DB 부하가 줄고 성능이 좋아지나 낮을 경우, 캐시가 잘못 저장됐거나, TTL이 너무 짧을 가능성이 있음
##### 시계열 데이터베이스: 시간을 기반으로 하는 데이터(메트릭, 로그, 이벤트 등)를 저장하고 빠르게 조회할 수 있는 DB 종류
##### 메트릭(metric): 측정 가능한 시스템의 성능 지표로 서버, 데이터베이스, 애플리케이션의 상태를 숫자로 나타낸 값(예: CPU 사용률: 70%, 메모리 사용량: 8GB, Redis 요청 수: 5,000 req/sec, DB 쿼리 응답 속도: 10ms)


### 1-3. Replication


## 2. Webflux

 

## 4. 접속자 대기 시스템 개발





### 📚참고자료
[9개 프로젝트로 경험하는 대용량 트래픽 & 데이터 처리 초격차 패키지 Online](https://fastcampus.co.kr/dev_online_traffic_data)  
[Redis란 무엇일까? - Redis의 특징과 사용 시 주의점](https://velog.io/@wnguswn7/Redis%EB%9E%80-%EB%AC%B4%EC%97%87%EC%9D%BC%EA%B9%8C-Redis%EC%9D%98-%ED%8A%B9%EC%A7%95%EA%B3%BC-%EC%82%AC%EC%9A%A9-%EC%8B%9C-%EC%A3%BC%EC%9D%98%EC%A0%90)  
[[Redis] 레디스 알고 쓰자. - 정의, 저장방식, 아키텍처, 자료구조, 유효 기간](https://velog.io/@banggeunho/%EB%A0%88%EB%94%94%EC%8A%A4Redis-%EC%95%8C%EA%B3%A0-%EC%93%B0%EC%9E%90.-%EC%A0%95%EC%9D%98-%EC%A0%80%EC%9E%A5%EB%B0%A9%EC%8B%9D-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98-%EC%9E%90%EB%A3%8C%EA%B5%AC%EC%A1%B0-%EC%9C%A0%ED%9A%A8-%EA%B8%B0%EA%B0%84)  
[[Memory] cache란? 캐시(cache)의 동작원리, 캐싱전략(caching strategies)](https://joyhong-91.tistory.com/26)  
[프로메테우스(소프트웨어)](https://en.wikipedia.org/wiki/Prometheus_(software))  
[오픈소스 모니터링 시스템 Prometheus #1](https://blog.outsider.ne.kr/1254)  
[Grafana란?](https://medium.com/finda-tech/grafana%EB%9E%80-f3c7c1551c38)
