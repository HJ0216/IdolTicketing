package org.example;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Publisher {
  public Flux<Integer> startFlux(){
    return Flux.range(1, 5).log();
  }

  public Flux<String> startFluxString(){
    return Flux.fromIterable(List.of("a", "b", "c")).log();
  }

  public Mono<Integer> startMono(){
    return Mono.just(1).log();
  }

  public Mono<Object> startMonoEmpty(){
    return Mono.empty().log();
  }

  public Mono<Object> startMonoError(){
    return Mono.error(new Exception("Hello World!")).log();
  }
}
