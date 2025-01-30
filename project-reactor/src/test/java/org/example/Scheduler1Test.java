package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class Scheduler1Test {
  private Scheduler1 scheduler = new Scheduler1();

  @Test
  void fluxMapWithSubscribeOn() {
    StepVerifier.create(scheduler.fluxMapWithSubscribeOn())
        .expectNextCount(10)
        .verifyComplete();
  }

  @Test
  void fluxSubScheduler1() {
    StepVerifier.create(scheduler.fluxSubScheduler1())
                .expectNextCount(10)
                .verifyComplete();
  }

  @Test
  void fluxSubScheduler2() {
    StepVerifier.create(scheduler.fluxSubScheduler2())
                .expectNextCount(10)
                .verifyComplete();
  }

  @Test
  void fluxPubScheduler1() {
    StepVerifier.create(scheduler.fluxPubScheduler1())
                .expectNextCount(10)
                .verifyComplete();
  }

  @Test
  void fluxPubScheduler2() {
    StepVerifier.create(scheduler.fluxPubScheduler2())
                .expectNextCount(10)
                .verifyComplete();
  }

}