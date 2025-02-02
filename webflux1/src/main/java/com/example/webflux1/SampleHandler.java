package com.example.webflux1;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class SampleHandler {

  public Mono<ServerResponse> getString(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Hello, Functional Endpoint!");

    // handlerFunction은 반드시 ServerRequest -> Mono<ServerResponse> 형태
    // ServerRequest를 매개변수로 안 받으면 Spring이 이걸 핸들러로 인식 못함
  }

}
