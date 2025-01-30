package org.example;

import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Operator4 {
  // limit
  public Flux<Integer> fluxDelayAndLimit() {
    return Flux.range(1, 10)
               .delaySequence(Duration.ofSeconds(1))
               .limitRate(2)
               .log();

    // limitRate: 소비되는 데이터의 속도를 제한하는 연산자
    // 소비자에게 한 번에 2개의 숫자만 전달되고, 그 후에는 다시 요청을 받아서 2개씩 전달
    // 예를 들어, 1, 2가 1초 간격으로 출력되고, 그 후에 3, 4가 또 1초 간격으로 출력되는 방식으로 속도 제한이 적용
    // 백프레셔(Backpressure): 데이터 흐름을 제어하는 메커니즘으로, 주로 비동기 스트림 처리에서 데이터가 너무 빨리 들어오는 상황을 관리하는 데 사용
    // 소비자가 처리할 수 있는 만큼만 데이터를 받도록 하여, 과부하가 발생하지 않도록 조절
  }

  // sample
  public Flux<Integer> fluxSample() {
    return Flux.range(1, 100)
               .delayElements(Duration.ofMillis(100))
               .sample(Duration.ofMillis(300))
               .log();

    // samle:  데이터를 주기적으로 샘플링
    // 300ms마다 스트림에서 최근 값을 선택하여 그 값을 반환
  }
}
