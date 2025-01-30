package org.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Operator3 {
  // count
  public Mono<Long> fluxCount(){
    return Flux.range(1, 10)
               .count()
               .log();
  }

  // distinct
  public Flux<String> fluxDistinct(){
    return Flux.just("a", "b", "c", "a", "A", "b")
               .distinct()
               .log();
  }

  // reduce
  public Mono<Integer> fluxReduce() {
    return Flux.range(1, 10)
               .reduce((i, j) -> i + j)
               .log();
  }

  // groupby
    public Flux<Integer> fluxGroupBy() {
      return Flux.range(1, 10)
                 .groupBy(i -> (i % 2 == 0) ? "even" : "odd")
                 .flatMap(group -> group.reduce((i, j) -> i + j))
                 .log();

      // flatMap: 각 그룹에 대해 변환을 적용
      // group: 각 그룹의 Flux 객체
    }
}
