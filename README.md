# 데이터 베이스 최적화를 통한 대용량 트래픽 처리
**📺강의:** ***[아이돌 티켓팅 접속자 대기열 시스템](https://fastcampus.co.kr/dev_online_traffic_data)***  
**📑블로그:** ***[아이돌 티켓팅 접속자 대기열 시스템](https://hj0216.tistory.com/category/MinimiProject/%EC%95%84%EC%9D%B4%EB%8F%8C%20%ED%8B%B0%EC%BC%93%ED%8C%85%20%EC%A0%91%EC%86%8D%EC%9E%90%20%EB%8C%80%EA%B8%B0%EC%97%B4%20%EC%8B%9C%EC%8A%A4%ED%85%9C)***

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
* Replication
  *  master-slave 아키텍처를 사용하여 master 데이터를 실시간으로 여러 slave 서버에 비동기 복제
  * 필요성
    * 안정성  
     데이터 손실 방지 및 master 서버 장애 발생 시, slave 서버를 데이터가 유지
    * 가용성  
      master 서버가 다운되더라도 slave 서버를 통해 읽기 작업을 처리
      * Failover  
        기존 master 중단 후, slave 중 하나가 master 승격  
        → 기존 master 재시작 시에도 slave 역할로 동작  
        → 데이터 일관성을 유지(master를 대리했던 slave가 새로운 정보를 Write 했을 가능성이 존재)
    * 읽기 부하 분산  
      여러 개의 slave 서버를 운영하여 읽기 부하를 분산하여 성능 개선 및 대규모 트래픽 처리 가능



## 2. Spring Webflux
* Spring Webflux
  * 비동기 논블로킹 방식의 Reactive Programming을 지원하는 Spring Framework의 모듈
  * Reactor(Core) 라이브러리의 Flux와 Mono를 활용하여 데이터를 비동기 스트림으로 처리
  * 특징
    * 비동기/논블로킹: 요청과 응답이 독립적으로 처리
    * 리액티브 스트림 기반: Publisher-Subscriber 패턴 활용
      * Publisher: 데이터를 한 번에 다 주는 게 아니라 필요할 때마다 Mono<T>, Flux<T> 형태로 제공
      * Subscriber: Publisher로부터 데이터를 비동기적으로 받아서 처리
    * Netty, Undertow, Tomcat 등 지원
    * Backpressure 지원: Publisher가 끊임없이 emit하는 무수히 많은 데이터를 적절하게 제어하여 데이터 처리에 과부하가 걸리지 않도록 제어
    * WebFlux는 높은 동시성을 제공하지만, 단순한 CRUD API에서는 MVC보다 성능이 낮을 수 있음 (스레드 컨텍스트 스위칭 비용 발생)
    * 실시간 데이터 처리(예: 채팅, IoT)에 주로 사용
* Spring MVC
  * 서블릿 기반의 동기 요청-응답 처리 모델  
  각 요청이 서블릿 스레드 풀에서 하나의 스레드를 점유하며, 응답이 완료될 때까지 해당 스레드는 블로킹됨
  * 특징
    * 동기식 처리: 요청과 응답이 1:1로 매핑되어 순차적으로 처리
    * Tomcat (Servlet 기반) 지원
    * Servlet API 기반: DispatcherServlet을 중심으로 작동
    * 전통적인 REST API와 CRUD 시스템에서 주로 사용
 
 <br/>

**Spring WebFlux vs Spring MVC 비교**

| 항목                | Spring WebFlux                  | Spring MVC                    |
|---------------------|---------------------------------|------------------------------|
| **요청 처리 방식**   | 비동기/논블로킹                  | 동기/블로킹                    |
| **멀티스레드 모델**  | 이벤트 루프 기반                 | 서블릿 스레드 풀 기반           |
| **데이터 처리 방식** | 리액티브 스트림 (`Flux`, `Mono`) | 일반적인 `List`, `Map` 반환    |
| **적합한 사용 사례** | 실시간 데이터, 스트리밍, IoT 등   | CRUD 서비스, 전통적인 REST API |
| **서버 엔진**       | Netty, Tomcat (Reactive 기반)    | Tomcat, Jetty (Servlet 기반)  |
| **Backpressure**    | O                               | X                             |


## 3. 접속자 대기 시스템 개발
[flow](https://github.com/HJ0216/IdolTicketing/tree/main/flow) 및 [website](https://github.com/HJ0216/IdolTicketing/tree/main/website) 프로젝트 참고


## 4. 배운점
1. Docker  
* 정의: 리눅스 컨테이너 기술을 기반으로, 애플리케이션을 독립적이고 격리된 환경에서 실행할 수 있게 지원하는 플랫폼
* 장점: 애플리케이션 실행에 필요한 모든 라이브러리와 설정을 이미지에 포함하여, 개발, 테스트, 운영 환경 간 일관성을 유지 → 환경 차이로 인한 오류를 줄임  
도커의 가상화 방식은 새롭게 커널 OS를 생성하지 않고 호스트 OS의 커널 자원을 공유하여 사용 → 가볍고 효율적이며, 실행 속도가 빠름
* 구성
  * Docker Image: 컨테이너를 생성하기 위한 파일 시스템과 설정이 포함된 템플릿
  * Docker Container: 이미지를 기반으로 생성된 독립적이고 격리된 실행 환경, 애플리케이션 실행에 필요한 라이브러리와 종속성 등을 포함하고 있음, 이미지를 기반으로 한 하나의 애플리케이션  
  * Docker Volume: 내부 데이터를 컨테이너 외부에 저장할 수 있게 해주는 데이터 관리 방식(일반적으로 컨테이너에서 생긴 데이터들은 내부에서 관리되며 컨테이너가 삭제될 때 같이 삭제되지만, 볼륨을 사용하면 데이터는 컨테이너와 독립적으로 저장됨)
  * Docker Network: 도커 컨테이너들이 서로 통신할 수 있게 해주는 가상 네트워크 시스템, 여러 컨테이너가 같은 네트워크 내에서 서로 통신할 수 있도록 관리
* Docker Compose: 여러 개의 도커 컨테이너를 정의하고 실행하기 위한 도구  
하나의 설정 파일로 여러 개의 컨테이너를 관리하고, 컨테이너 간의 네트워크 및 종속성을 설정하는 데 사용

<br/>

2. Spring Data Redis
* 정의: Spring Data Redis provides easy configuration and access to Redis from Spring applications.
* Drivers
  * Lettuce
    * a Netty-based open-source connector supported by Spring Data Redis
    * 비동기(Async) 및 반응형(Reactive) 지원 → WebFlux, R2DBC와 함께 사용 가능
    * Netty 기반의 효율적인 I/O 모델 → 높은 확장성
      * Netty: 비동기, 이벤트 기반의 네트워크 프레임워크
      * Java의 기본적인 I/O 방식(예: 블로킹 소켓)과 다르게, 비동기 방식으로 데이터 송수신을 처리  
      네트워크 요청이 들어올 경우, 하나의 쓰레드가 모든 요청을 관리하는 것이 아니라 이벤트 루프(들어오는 요청을 이벤트 큐에 저장하고, 요청을 처리하는 동안 I/O 작업이 끝나길 기다리는 게 아니라, 다른 요청을 처리하고 있다가, 결과가 준비되면 콜백을 실행하는 방식)를 통해 처리
    * Jedis보다 상대적으로 설정이 복잡하며 네이티브 Redis 명령어 지원이 일부 제한될 수 있음
    * ***Spring WebFlux, R2DBC를 사용하는 경우 적합***
  * Jedis: a community-driven connector supported by the Spring Data Redis module
    * 사용이 간단하고 설정이 쉬움
    * 블로킹 방식 → WebFlux 등과 함께 사용하기 어려움
    * ***Spring MVC와 동기 방식이 필요한 경우 적합***
* RedisTemplate
  * 다양한 Redis 명령어를 쉽게 사용할 수 있도록 도와주는 템플릿 클래스
  * String, Hash, List, Set, Sorted Set 등 다양한 Redis 데이터 타입을 지원
  * Redis 명령어를 직접 호출할 수 있어, 캐시 TTL 설정, 트랜잭션 관리 등 상세한 조작 가능
  * Redis는 기본적으로 byte[] 데이터를 저장하므로, (De)Serialization을 명확히 설정해야 함
  * ***Redis의 다양한 데이터 구조를 활용하거나 세부적인 Redis 기능을 직접 컨트롤해야 할 때, WebFlux 환경에서 Reactive Redis를 사용하고 싶을 때 사용***
* RedisRepository
  * JpaRepository와 유사한 인터페이스 기반의 Redis 데이터 접근 방식을 제공
  * Redis의 Hash 데이터 구조를 활용하여 기본적인 CRUD를 자동으로 처리할 수 있음
  * RDB와 유사한 방식으로 데이터를 저장하고 관리하고 싶을 때 사용
  * ***데이터를 Hash 구조로 저장하고 조회 성능을 최적화하고 싶을 때, Entity 스타일로 데이터를 저장하고 싶을 때 사용***
* Cache
  * 자주 사용하는 데이터를 미리 저장해두고 빠르게 가져오는 메모리 기반 저장소
  * 메모리에 데이터를 저장하므로 디스크 I/O를 줄여 빠른 읽기/쓰기가 가능
  * 각 캐시 항목마다 유효 기간을 설정할 수 있어, 일정 시간이 지나면 자동으로 삭제되어 메모리 관리를 도와줌
  * ***적합한 사용 사례***
    * 자주 조회되는 데이터 캐싱
    * 세션 관리
    * 실시간 데이터 처리
    * 부하 분산
  * 주의 사항
    * 캐시와 실제 데이터베이스 사이의 데이터 일관성을 맞추기 위한 적절한 캐시 무효화 전략이 필요
    * 서버가 재부팅되거나 Redis 프로세스가 비정상 종료되면 데이터가 유실될 위험이 존재
* Pipelining
  * 여러 개의 Redis 명령어를 한 번에 전송하여 성능을 향상시키는 기능
  * 기본적으로 Redis는 클라이언트가 하나의 요청을 보내고 응답을 받은 후 다음 요청을 보내는 방식(요청-응답 패턴)으로 동작하지만, Pipeline을 사용하면 여러 개의 명령을 한 번에 전송하고, Redis가 처리한 후 한꺼번에 응답을 받을 수 있음
  * 이전 명령의 결과를 다음 명령에서 사용할 수 없으므로 명령어 간 의존성이 있으면 사용하기 어려움
  * 모든 응답을 한 번에 받아야 하므로, 큰 데이터 처리는 주의
* Replication
  * *정의*: 하나의 마스터 서버가 여러 개의 슬레이브 서버에 데이터를 복제하여 동기화하는 기능
  * *목적*
    * 안정성: 마스터 서버에서 발생한 데이터 변경이 슬레이브 서버로 자동으로 복제  
    → 데이터 손실을 방지하고, 마스터 서버가 장애를 겪었을 때 슬레이브 서버를 통해 데이터가 유지될 수 있도록 함
    * 가용성:  여러 서버에 데이터를 복제함으로써, 마스터 서버가 다운되더라도 슬레이브 서버를 통해 읽기 작업을 처리할 수 있음
    * 읽기 부하 분산: 마스터 서버에 대한 읽기 요청을 슬레이브 서버로 분산시킬 수 있음  
    → 여러 개의 슬레이브 서버를 운영하여 읽기 부하를 분산시킴으로써 성능을 개선하고, 대규모 트래픽을 처리할 수도 있음

<br/>

3. 비기능 테스트(Non-functional Testing)
* 종류
![Non-functional-testing](https://github.com/user-attachments/assets/b9c650e0-9d0e-42bc-8067-f5b9e986fa01)
  * 성능 테스트(Performance Test)
    * *정의*: 특정 상황에서 시스템의 성능을 검증하는 테스트
    * *목적*: 시스템의 응답 시간, 처리량, 자원 사용량, 확장성, 신뢰성 등을 검증
  * 부하 테스트(Load Test)
    * *정의*: 시스템에 점진적으로 부하(유저 수, 초당 API 요청 수, 데이터 처리량 등)를 증가시키면서 성능과 안정성을 검증하는 테스트
    * *목적*: 시스템의 최대 처리량과 임계값 식별 및 성능 병목 구간 탐색
  * 스트레스 테스트(Stress Test)
    * *정의*: 정상적인 처리량을 초과하는 과부하를 가하여 시스템의 한계 및 복구 능력을 검증하는 테스트
    * *목적*: 시스템의 복구 능력, 장애 대응 조치 수행 여부, 보안상의 문제 발생(서비스 거부 공격 등) 여부 등을 검증
  * 내구성 테스트(Endurance Test)
    * *정의*: 장시간 동안 지속적인 부하를 가하여 시스템의 안정성을 검증하는 테스트
    * *목적*: 메모리 누수/자원 고갈 여부 확인, 장기 실행 시 성능 저하 여부 평가, CPU, 네트워크 사용량의 누적적 영향 등을 분석
  * 스파이크 테스트(Spike Test)
    * *정의*: 짧은 시간 동안 갑작스러운 높은 부하를 가하여 시스템의 반응성과 복구 능력을 검증
    * *목적*: 대규모 트래픽이 순간적으로 유입될 때의 시스템의 처리 능력과 정상 상태로 복구 여부 등을 평가
  * 확장성 테스트(Scalability Test)
    * *정의*: 시스템이 증가하는 부하에 맞춰 확장될 수 있는지를 검증하는 테스트
    * *목적*: 시스템이 수직적(스케일 업) 또는 수평적(스케일 아웃)으로 확장 가능한지 확인
      * Scale-up: 스케일 업은 기존 서버의 사양을 업그레이드해 시스템을 확장하는 것
      * Scale-out:  서버를 여러 대 추가하여 시스템을 확장하는 것
  * 복구 테스트(Recovery Test)
    * 장애 발생 후 시스템이 정상 상태로 복구되는지를 검증하는 테스트
    * 장애 발생 시 자동 복구(예: 재시작, 페일오버 등)가 정상적으로 수행되는지 평가
      * Failover: 컴퓨터 서버, 시스템, 네트워크 등에서 이상이 생겼을 때 예비 시스템으로 자동전환되는 기능
      * Failback: Failover로 백업 서버에서 변경된 데이터를 동기화하고 장애가 발생하기 전의 본 서버로 되돌리는 기능

<br/>

4. 모니터링: Prometheus와 Grafana
* Monitoring
  * *정의*: 시스템의 상태를 지속적으로 관찰하고 측정하는 과정
  * *필요성*
    * 장애 발생 전 경고를 받고, 문제가 발생했을 때 신속한 대응 가능
    * 병목 현상, 리소스 부족 등을 분석하여 효율적인 최적화 가능
    * 중요한 서비스가 중단되지 않도록 예방 가능
* Prometheus
  * 이벤트 모니터링 및 알림에 사용되는 무료 소프트웨어 애플리케이션
  * 기본적으로 **Pull 방식**을 사용(Push 방식 일부 활용 가능)하여 구축된 **시계열 데이터베이스**에 **메트릭**을 기록
    * 다른 대부분의 모니터링 도구가 **Push 방식**을 사용
  * Exporter(Node Exporter, Redis Exporter 등)를 활용하여 다양한 데이터 수집 가능
    * Node Exporter: CPU 사용률 / 메모리 사용량 / 디스크 I/O / 네트워크 트래픽 (+Grafana: 서버 대시보드)
    * Redis Exporter: 활성 클라이언트 / 명령어 요청 수 / 메모리 사용량 / 키 만료 수 (+Grafana: Redis 대시보드)
    * PostgreSQL Exporter: 실행 중인 쿼리 / 연결 수 / 롤백된 트랜잭션 수 (+Grafana: PostgreSQL 대시보드)
    * NGINX Exporter: HTTP 요청 수 / 500 에러 비율 / 현재 접속자 수 (+Grafana: NGINX 대시보드)
* Grafana
  * 시계열 매트릭, 로그 데이터 등을 시각화 하는데 최적화된 대시보드를 제공해주는 오픈소스 도구
  * Prometheus, Elasticsearch, MySQL 등 다양한 데이터 소스와 연동하여 사용자 정의 대시보드를 통해 시각화하는데 주로 사용

##### Pull 방식: Prometheus 서버가 설정된 대상(Exporter, 애플리케이션 등)에 주기적으로 요청을 보내어 데이터를 가져오는 방식
##### 시계열 데이터베이스: 시간을 기반으로 하는 데이터(메트릭, 로그, 이벤트 등)를 저장하고 빠르게 조회할 수 있는 DB 종류
##### 메트릭(metric): 측정 가능한 시스템의 성능 지표로 서버, 데이터베이스, 애플리케이션의 상태를 숫자로 나타낸 값 <br/> (예: CPU 사용률: 70%, 메모리 사용량: 8GB, Redis 요청 수: 5,000 req/sec, DB 쿼리 응답 속도: 10ms)
##### Push 방식: 각 서버에 에이전트를 설치하여 모니터링 서버로 데이터를 전송하는 방식

<br/>

5. Spring WebFlux vs MVC
* Spring WebFlux
  * 비동기 논블로킹 방식의 Reactive Programming을 지원하는 Spring Framework의 모듈
  * Reactor(Core) 라이브러리의 Flux와 Mono를 활용하여 데이터를 비동기 스트림으로 처리
  * 특징
    * 비동기/논블로킹: 요청과 응답이 독립적으로 처리
    * 리액티브 스트림 기반: Publisher-Subscriber 패턴 활용
      * Publisher: 데이터를 한 번에 다 주는 게 아니라 필요할 때마다 Mono<T>, Flux<T> 형태로 제공
      * Subscriber: Publisher로부터 데이터를 비동기적으로 받아서 처리
    * **Netty**, Undertow, Tomcat 등 지원
    * Backpressure 지원: Publisher가 끊임없이 emit하는 무수히 많은 데이터를 적절하게 제어하여 데이터 처리에 과부하가 걸리지 않도록 제어
    * WebFlux는 높은 동시성을 제공하지만, 단순한 CRUD API에서는 MVC보다 성능이 낮을 수 있음 (스레드 컨텍스트 스위칭 비용 발생)
    * 실시간 데이터 처리(예: 채팅, IoT)에 주로 사용
* Spring MVC
  * **Servlet** 기반의 동기 요청-응답 처리 모델  
  각 요청이 Servlet 스레드 풀에서 하나의 스레드를 점유하며, 응답이 완료될 때까지 해당 스레드는 블로킹됨
  * 특징
    * 동기식 처리: 요청과 응답이 1:1로 매핑되어 순차적으로 처리
    * **Tomcat** (Servlet 기반) 지원
    * Servlet API 기반: DispatcherServlet을 중심으로 작동
    * 전통적인 REST API와 CRUD 시스템에서 주로 사용
 
 <br/>

**Spring WebFlux vs Spring MVC 비교**

| 항목                | Spring WebFlux                  | Spring MVC                    |
|---------------------|---------------------------------|------------------------------|
| **요청 처리 방식**   | 비동기/논블로킹                  | 동기/블로킹                    |
| **멀티스레드 모델**  | **이벤트 루프** 기반             | Servlet 스레드 풀 기반           |
| **데이터 처리 방식** | 리액티브 스트림 (`Flux`, `Mono`) | 일반적인 `List`, `Map` 반환    |
| **적합한 사용 사례** | 실시간 데이터, 스트리밍, IoT 등   | CRUD 서비스, 전통적인 REST API |
| **서버 엔진**       | Netty, Tomcat (Reactive 기반)    | Tomcat, Jetty (Servlet 기반)  |
| **Backpressure**    | O                               | X                             |

##### Netty: 비동기, 이벤트 기반의 네트워크 프레임워크
##### Tomcat: Java Servlet과 JSP(HTML 내에 Java 코드를 삽입하여 동적으로 웹 페이지를 생성할 수 있도록 하는 기술)를 실행하는 동기 기반의 서블릿 컨테이너
##### Servlet: Java에서 웹 요청과 응답을 처리하기 위한 표준 인터페이스 및 API로 동기 요청-응답 처리 방식
##### Servlet Container: Servlet을 실행하고 요청을 처리하는 서버 환경(Servlet의 실행 및 생명주기(생성, 초기화, 요청 처리, 종료 등)를 관리)
##### Event Loop: 들어오는 요청을 이벤트 큐에 저장하고, 요청을 처리하는 동안 I/O 작업이 끝나길 기다리는 게 아니라, 다른 요청을 처리하고 있다가, 결과가 준비되면 콜백을 실행하는 방식

<br/>

6. 기타
* Enum을 활용한 ErrorCode
  ```java
  @AllArgsConstructor
  public enum ErrorCode {
    QUEUE_ALREADY_REGISTERED_USER_IN_QUEUE(HttpStatus.CONFLICT, "UQ-0002", "Already Registered in Queue-%s");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    public ApplicationException build(Object ...args){
      return new ApplicationException(status, code, reason.formatted(args));
    }
  }
  ```
* record 타입
  * 불변(immutable) 데이터 객체를 간단하게 정의하기 위한 새로운 클래스 유형으로, Java 16부터 정식 기능으로 사용
  * getter, toString(), equals(), hashCode() 등을 자동으로 생성
  * 데이터 전달용 객체(DTO)나 불변 객체를 만들 때 사용
* Scheduling
  * @Scheduled 어노테이션을 활용하여 별도의 외부 라이브러리 없이, 특정 시간마다 작업을 실행하는 배치 작업을 간단하게 설정할 수 있음

<br/>



### 📚참고자료
---
[9개 프로젝트로 경험하는 대용량 트래픽 & 데이터 처리 초격차 패키지 Online](https://fastcampus.co.kr/dev_online_traffic_data)  
[Redis란 무엇일까? - Redis의 특징과 사용 시 주의점](https://velog.io/@wnguswn7/Redis%EB%9E%80-%EB%AC%B4%EC%97%87%EC%9D%BC%EA%B9%8C-Redis%EC%9D%98-%ED%8A%B9%EC%A7%95%EA%B3%BC-%EC%82%AC%EC%9A%A9-%EC%8B%9C-%EC%A3%BC%EC%9D%98%EC%A0%90)  
[[Redis] 레디스 알고 쓰자. - 정의, 저장방식, 아키텍처, 자료구조, 유효 기간](https://velog.io/@banggeunho/%EB%A0%88%EB%94%94%EC%8A%A4Redis-%EC%95%8C%EA%B3%A0-%EC%93%B0%EC%9E%90.-%EC%A0%95%EC%9D%98-%EC%A0%80%EC%9E%A5%EB%B0%A9%EC%8B%9D-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98-%EC%9E%90%EB%A3%8C%EA%B5%AC%EC%A1%B0-%EC%9C%A0%ED%9A%A8-%EA%B8%B0%EA%B0%84)  
[[Memory] cache란? 캐시(cache)의 동작원리, 캐싱전략(caching strategies)](https://joyhong-91.tistory.com/26)  
[프로메테우스(소프트웨어)](https://en.wikipedia.org/wiki/Prometheus_(software))  
[오픈소스 모니터링 시스템 Prometheus #1](https://blog.outsider.ne.kr/1254)  
[Grafana란?](https://medium.com/finda-tech/grafana%EB%9E%80-f3c7c1551c38)  
[[리액티브 프로그래밍] Backpressure의 개념과 Backpressure 전략](https://devfunny.tistory.com/914)  
[[docker란] 도커를 선택할 수 밖에 없는 이유](https://www.elancer.co.kr/blog/detail/757)  
[Spring Data Redis](https://spring.io/projects/spring-data-redis)  
[RedisRepository 및 RedisTemplate에 대해](https://velog.io/@eora21/RedisTemplate-%EB%B0%8F-RedisRepository%EC%97%90-%EB%8C%80%ED%95%B4)  
[성능테스트, 부하테스트, 스트레스 테스트..무엇이 다를까?](https://seongwon.dev/ETC/20220919-%EC%84%B1%EB%8A%A5%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B6%80%ED%95%98%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%8A%A4%ED%8A%B8%EB%A0%88%EC%8A%A4%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%9E%80/#reference)  
[성능 테스트, 부하 테스트, 스트레스 테스트 차이](https://dgjinsu.tistory.com/61)  
[성능 테스트 유형 알아보기](https://engineering-skcc.github.io/performancetest/Performance-Testing-Terminologies/)  
