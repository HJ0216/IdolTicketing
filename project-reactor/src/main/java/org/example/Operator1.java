package org.example;

import java.time.Duration;
import reactor.core.publisher.Flux;

public class Operator1 {
  // map
  public Flux<Integer> fluxMap(){
    return Flux.range(1, 5)
        .map(i -> i * 2)
        .log();
  }

  // filter
  public Flux<Integer> fluxFilter(){
    return Flux.range(1, 10)
               .filter(i -> i % 2 == 0)
               .log();
  }

  // take
  public Flux<Integer> fluxFilterTake(){
    return Flux.range(1, 10)
               .filter(i -> i > 5)
               .take(3)
               .log();
  }

  // flatMap
  public Flux<Integer> fluxFlatMap(){
    return Flux.range(1, 10)
               .flatMap(i -> Flux.range(i * 10, 10)
               .delayElements(Duration.ofMillis(10)))
               .log();
  }
  public Flux<Integer> fluxFlatMap2(){
    return Flux.range(1, 9)
               .flatMap(i -> Flux.range(1, 9)
                   .map(j -> {
                     System.out.printf("%d * %d = %d\n", i, j, i*j);
                     return i*j;
                   })
               );
//               .log();

    // Flux.range(1, 9) → 1부터 9까지 i를 emit
    // Flux.range(1, 9) → 1부터 9까지 j를 emit
    // map(j -> i * j) → 구구단 결과 계산 및 출력
    // 결과를 Flux<Integer>로 반환

    // flatMap()은 여러 Flux를 비동기적으로 처리할 수 있어서 결과값의 순서를 보장할 수 없음.
    // 하지만, 현재 코드에서는 내부 Flux들이 동기적으로 실행되므로 flatMap 결과값의 순서가 항상 보장됨.
  }

}
