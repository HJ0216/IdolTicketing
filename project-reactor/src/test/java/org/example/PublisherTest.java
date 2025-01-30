package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class PublisherTest {
  private Publisher publisher = new Publisher();

  @Test
  void startFlux() {
    StepVerifier.create(publisher.startFlux())
        .expectNext(1,2,3,4,5)
        .verifyComplete();
  }

  @Test
  void startFluxString() {
    StepVerifier.create(publisher.startFluxString())
        .expectNext("a", "b", "c")
        .verifyComplete();
  }

  @Test
  void startMono() {
    StepVerifier.create(publisher.startMono())
        .expectNext(1)
        .verifyComplete();
  }

  @Test
  void startMonoEmpty() {
    StepVerifier.create(publisher.startMonoEmpty())
        .verifyComplete();
  }

  @Test
  void startMonoError() {
    StepVerifier.create(publisher.startMonoError())
        .expectError(Exception.class)
        .verify();
  }
}