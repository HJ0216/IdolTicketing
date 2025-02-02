package com.example.webflux1.service;

import com.example.webflux1.client.PostClient;
import com.example.webflux1.dto.PostResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class PostService {
  // webclient 기반 mvc server request

  private final PostClient postClient;

  public Mono<PostResponse> getPostContent(Long id) {
    return postClient.getPost(id)
        .onErrorResume(error -> Mono.just(new PostResponse(id.toString(), error.getMessage())));

    //  onErrorResume
    //  Flux 또는 Mono 흐름에서 발생한 에러를 처리하고 에러가 발생한 경우 대체 값을 반환: fallback(대체 동작)을 설정해 주는 역할
    // onErrorResume x: 일부 데이터 값 오류 시, 응답 X
    // onErrorResume o: 일부 데이터 값 오류 시, 응답 O
  }

  public Flux<PostResponse> getMultiplePostContent(List<Long> ids) {
    return Flux.fromIterable(ids)
        .flatMap(this::getPostContent)
        .log();
  }

  public Flux<PostResponse> getParallelMultiplePostContent(List<Long> ids) {
    return Flux.fromIterable(ids)
               .parallel() // 데이터를 병렬 처리할 수 있도록 나누는 역할(데이터를 병렬로 처리할 수 있도록 Flux 스트림을 변환)
               .runOn(Schedulers.parallel()) // 병렬 작업을 실행할 스레드를 지정하는 역할
               .flatMap(this::getPostContent) // 비동기적인 작업 처리 후, 결과를 단일 스트림으로 변환
               .log()
               .sequential(); // 병렬 처리로 나누어졌던 스트림을 다시 순차적인 스트림으로 합치는 역할

    // 데이터를 3개만 요청하더라도 쓰레드가 3개만 만들어지는 게 아니라 여러개의 쓰레드가 만들어져서 그 중 일부가 사용됨
  }
}
