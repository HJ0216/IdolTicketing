package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class Operator2Test {
  private Operator2 op = new Operator2();

  @Test
  void fluxConcatMap() {
    StepVerifier.create(op.fluxConcatMap())
        .expectNextCount(100)
        .verifyComplete();
  }

  @Test
  void monoFlatMapMany() {
    StepVerifier.create(op.monoFlatMapMany())
        .expectNextCount(10)
        .verifyComplete();
  }

  @Test
  void monoDefaultIfEmpty() {
    StepVerifier.create(op.monoDefaultIfEmpty())
                .expectNext(30)
                .verifyComplete();
  }

  @Test
  void monoSwitchIfEmpty() {
    StepVerifier.create(op.monoSwitchIfEmpty())
        .expectNext(20)
        .verifyComplete();
  }

  @Test
  void monoSwitchIfEmpty2() {
    StepVerifier.create(op.monoSwitchIfEmpty2())
        .expectError(Exception.class)
        .verify();
  }

  @Test
  void fluxMerge() {
    StepVerifier.create(op.fluxMerge())
                .expectNext("a", "c", "e", "b", "d")
                .verifyComplete();
  }

  @Test
  void monoMerge() {
    StepVerifier.create(op.monoMerge())
                .expectNext("1", "2", "3")
                .verifyComplete();
  }

  @Test
  void fluxZip() {
    StepVerifier.create(op.fluxZip())
                .expectNext("ㄱㄹ", "ㄴㅁ", "ㄷㅂ")
                .verifyComplete();
  }

  @Test
  void monoZip() {
    StepVerifier.create(op.monoZip())
                .expectNext(6)
                .verifyComplete();
  }
}