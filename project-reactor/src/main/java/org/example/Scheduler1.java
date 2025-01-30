package org.example;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class Scheduler1 {
  public Flux<Integer> fluxMapWithSubscribeOn(){
    return Flux.range(1, 10)
        .map(i -> i * 2)
        .subscribeOn(Schedulers.boundedElastic())
        .log();

    // subscribeOn: 구독 시점에서 사용되는 스레드를 지정
    // subscribeOn은 한 번만 적용되며, 그 이후의 연산은 해당 스레드에서 계속 실행
    // boundedElastic: 유연하게 스레드를 관리하며, 다수의 I/O 작업을 효율적으로 처리할 수 있도록 도움
  }

  public Flux<Integer> fluxMapWithPublishOn(){
    return Flux.range(1, 10)
               .map(i -> i * 2)
               .publishOn(Schedulers.boundedElastic())
               .log()
               .publishOn(Schedulers.parallel())
               .log()
               .map(i -> i + 1)
               .log();

    // publishOn: 데이터가 처리되는 시점에서의 스레드를 지정
    // 이전에 실행된 연산이 어떤 스레드에서 실행됐는지는 상관하지 않고, 그 이후 연산만 변경된 스레드에서 실행됨
    // parallel: 멀티코어 CPU에서 병렬 처리에 적합한 스케줄러
  }

  public Flux<Integer> fluxSubScheduler1() {
    return Flux.range(1, 10)
               .subscribeOn(Schedulers.boundedElastic())
               .map(i -> i + 10)
               .log()
               .map(i -> i + 100)
               .log();

    /**
     * [ INFO] (Test worker) onSubscribe(FluxMap.MapSubscriber)
     * [ INFO] (Test worker) onSubscribe(FluxMap.MapSubscriber)
     * [ INFO] (Test worker) request(unbounded)
     * [ INFO] (Test worker) request(unbounded)
     * [ INFO] (boundedElastic-1) onNext(11)
     * [ INFO] (boundedElastic-1) onNext(111)
     * [ INFO] (boundedElastic-1) onNext(12)
     * [ INFO] (boundedElastic-1) onNext(112)
     * [ INFO] (boundedElastic-1) onNext(13)
     * [ INFO] (boundedElastic-1) onNext(113)
     * [ INFO] (boundedElastic-1) onNext(14)
     * [ INFO] (boundedElastic-1) onNext(114)
     * [ INFO] (boundedElastic-1) onNext(15)
     * [ INFO] (boundedElastic-1) onNext(115)
     * [ INFO] (boundedElastic-1) onNext(16)
     * [ INFO] (boundedElastic-1) onNext(116)
     * [ INFO] (boundedElastic-1) onNext(17)
     * [ INFO] (boundedElastic-1) onNext(117)
     * [ INFO] (boundedElastic-1) onNext(18)
     * [ INFO] (boundedElastic-1) onNext(118)
     * [ INFO] (boundedElastic-1) onNext(19)
     * [ INFO] (boundedElastic-1) onNext(119)
     * [ INFO] (boundedElastic-1) onNext(20)
     * [ INFO] (boundedElastic-1) onNext(120)
     * [ INFO] (boundedElastic-1) onComplete()
     * [ INFO] (boundedElastic-1) onComplete()
     * */
  }

  public Flux<Integer> fluxSubScheduler2() {
    return Flux.range(1, 10)
               .subscribeOn(Schedulers.boundedElastic())
               .map(i -> i + 10)
               .log()
               .subscribeOn(Schedulers.parallel())
               .map(i -> i + 100)
               .log();

    /**
     * [ INFO] (Test worker) onSubscribe(FluxMap.MapSubscriber)
     * [ INFO] (Test worker) request(unbounded)
     * [ INFO] (parallel-1) onSubscribe(FluxMap.MapSubscriber)
     * [ INFO] (parallel-1) request(unbounded)
     * [ INFO] (boundedElastic-1) onNext(11)
     * [ INFO] (boundedElastic-1) onNext(111)
     * [ INFO] (boundedElastic-1) onNext(12)
     * [ INFO] (boundedElastic-1) onNext(112)
     * [ INFO] (boundedElastic-1) onNext(13)
     * [ INFO] (boundedElastic-1) onNext(113)
     * [ INFO] (boundedElastic-1) onNext(14)
     * [ INFO] (boundedElastic-1) onNext(114)
     * [ INFO] (boundedElastic-1) onNext(15)
     * [ INFO] (boundedElastic-1) onNext(115)
     * [ INFO] (boundedElastic-1) onNext(16)
     * [ INFO] (boundedElastic-1) onNext(116)
     * [ INFO] (boundedElastic-1) onNext(17)
     * [ INFO] (boundedElastic-1) onNext(117)
     * [ INFO] (boundedElastic-1) onNext(18)
     * [ INFO] (boundedElastic-1) onNext(118)
     * [ INFO] (boundedElastic-1) onNext(19)
     * [ INFO] (boundedElastic-1) onNext(119)
     * [ INFO] (boundedElastic-1) onNext(20)
     * [ INFO] (boundedElastic-1) onNext(120)
     * [ INFO] (boundedElastic-1) onComplete()
     * [ INFO] (boundedElastic-1) onComplete()
     * */
  }

  public Flux<Integer> fluxPubScheduler1() {
    return Flux.range(1, 10)
               .publishOn(Schedulers.boundedElastic())
               .map(i -> i + 10)
               .log()
               .map(i -> i + 100)
               .log();

    /**
     * [ INFO] (Test worker) | onSubscribe([Fuseable] FluxMapFuseable.MapFuseableSubscriber)
     * [ INFO] (Test worker) | onSubscribe([Fuseable] FluxMapFuseable.MapFuseableSubscriber)
     * [ INFO] (Test worker) | request(unbounded)
     * [ INFO] (Test worker) | request(unbounded)
     * [ INFO] (boundedElastic-1) | onNext(11)
     * [ INFO] (boundedElastic-1) | onNext(111)
     * [ INFO] (boundedElastic-1) | onNext(12)
     * [ INFO] (boundedElastic-1) | onNext(112)
     * [ INFO] (boundedElastic-1) | onNext(13)
     * [ INFO] (boundedElastic-1) | onNext(113)
     * [ INFO] (boundedElastic-1) | onNext(14)
     * [ INFO] (boundedElastic-1) | onNext(114)
     * [ INFO] (boundedElastic-1) | onNext(15)
     * [ INFO] (boundedElastic-1) | onNext(115)
     * [ INFO] (boundedElastic-1) | onNext(16)
     * [ INFO] (boundedElastic-1) | onNext(116)
     * [ INFO] (boundedElastic-1) | onNext(17)
     * [ INFO] (boundedElastic-1) | onNext(117)
     * [ INFO] (boundedElastic-1) | onNext(18)
     * [ INFO] (boundedElastic-1) | onNext(118)
     * [ INFO] (boundedElastic-1) | onNext(19)
     * [ INFO] (boundedElastic-1) | onNext(119)
     * [ INFO] (boundedElastic-1) | onNext(20)
     * [ INFO] (boundedElastic-1) | onNext(120)
     * [ INFO] (boundedElastic-1) | onComplete()
     * [ INFO] (boundedElastic-1) | onComplete()
     * */
  }

  public Flux<Integer> fluxPubScheduler2() {
    return Flux.range(1, 10)
               .publishOn(Schedulers.boundedElastic())
               .map(i -> i + 10)
               .log()
               .publishOn(Schedulers.parallel())
               .map(i -> i + 100)
               .log();

    /**
     * [ INFO] (Test worker) | onSubscribe([Fuseable] FluxMapFuseable.MapFuseableSubscriber)
     * [ INFO] (Test worker) | onSubscribe([Fuseable] FluxMapFuseable.MapFuseableSubscriber)
     * [ INFO] (Test worker) | request(unbounded)
     * [ INFO] (Test worker) | request(256)
     * [ INFO] (boundedElastic-1) | onNext(11)
     * [ INFO] (boundedElastic-1) | onNext(12)
     * [ INFO] (parallel-1) | onNext(111)
     * [ INFO] (parallel-1) | onNext(112)
     * [ INFO] (boundedElastic-1) | onNext(13)
     * [ INFO] (parallel-1) | onNext(113)
     * [ INFO] (boundedElastic-1) | onNext(14)
     * [ INFO] (boundedElastic-1) | onNext(15)
     * [ INFO] (parallel-1) | onNext(114)
     * [ INFO] (parallel-1) | onNext(115)
     * [ INFO] (boundedElastic-1) | onNext(16)
     * [ INFO] (boundedElastic-1) | onNext(17)
     * [ INFO] (parallel-1) | onNext(116)
     * [ INFO] (parallel-1) | onNext(117)
     * [ INFO] (boundedElastic-1) | onNext(18)
     * [ INFO] (boundedElastic-1) | onNext(19)
     * [ INFO] (parallel-1) | onNext(118)
     * [ INFO] (boundedElastic-1) | onNext(20)
     * [ INFO] (parallel-1) | onNext(119)
     * [ INFO] (parallel-1) | onNext(120)
     * [ INFO] (boundedElastic-1) | onComplete()
     * [ INFO] (parallel-1) | onComplete()
     * */
  }

}
