package org.example;

import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Operator2 {

  // concatMap
  public Flux<Integer> fluxConcatMap(){
    return Flux.range(1, 10)
        .concatMap(i -> Flux.range(i*10, 10)
            .delayElements(Duration.ofMillis(10)))
        .log();
  }

  // flatMapMany: mono to flux
  public Flux<Integer> monoFlatMapMany(){
    return Mono.just(10)
               .flatMapMany(i -> Flux.range(1, i))
               .log();
  }

  // defaultIfEmpty, switchIfEmpty
  public Mono<Integer> monoDefaultIfEmpty(){
    // defaultIfEmpty: 값이 비어 있을 때 기본값을 반환
    // 기본값을 Mono 또는 Flux로 감싸서 반환
    // Mono<T>에서 T가 Mono 타입일 수도 있음, 그러나 defaultIfEmpty에 Mono를 넘겨주면 Mono<Mono<Integer>>와 같은 중첩된 Mono 객체가 되어 의도한 동작과 맞지 않아 오류 발생
    return Mono.just(100)
               .filter(i -> i > 100)
               .defaultIfEmpty(30)
               .log();
  }

  public Mono<Integer> monoSwitchIfEmpty(){
    // switchIfEmpty: 빈 Mono 또는 Flux일 때 다른 Mono 또는 Flux로 대체
    // 빈 Mono 또는 Flux일 때 다른 Mono나 Flux로 대체하는 기능을 제공하며, 대체하는 Mono나 Flux에는 추가적인 연산을 넣을 수 있
    return Mono.just(100)
               .filter(i -> i > 100)
               .switchIfEmpty(Mono.just(10).map(i -> i * 2))
               .log();
  }

  public Mono<Integer> monoSwitchIfEmpty2(){
    return Mono.just(100)
               .filter(i -> i > 100)
               .switchIfEmpty(Mono.error(new Exception("Not exist value...")))
               .log();
  }

  // merge, zip
  public Flux<String> fluxMerge(){
    return Flux.merge(Flux.fromIterable(List.of("a", "c", "e")), Flux.fromIterable(List.of("b", "d")))
        .log();
  }

  public Flux<String> monoMerge(){
    return Mono.just("1").mergeWith(Mono.just("2")).mergeWith(Mono.just("3"))
               .log();
  }

  public Flux<String> fluxZip(){
    return Flux.zip(Flux.just("ㄱ", "ㄴ", "ㄷ"), Flux.just("ㄹ", "ㅁ", "ㅂ"))
        .map(i -> i.getT1() + i.getT2())
        .log();
  }

  public Mono<Integer> monoZip(){
    return Mono.zip(Mono.just(1), Mono.just(2), Mono.just(3))
        .map(i -> i.getT1() + i.getT2() + i.getT3())
        .log();
  }

}
