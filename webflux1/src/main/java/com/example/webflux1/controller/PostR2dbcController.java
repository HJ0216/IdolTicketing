package com.example.webflux1.controller;

import com.example.webflux1.dto.PostR2dbcCreateRequest;
import com.example.webflux1.dto.PostR2dbcResponse;
import com.example.webflux1.dto.PostR2dbcUpdateRequest;
import com.example.webflux1.service.PostR2dbcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/posts")
public class PostR2dbcController {

  private final PostR2dbcService postR2dbcService;

  @PostMapping("")
  public Mono<PostR2dbcResponse> createPost(@RequestBody PostR2dbcCreateRequest request) {
    return postR2dbcService.create(request.getUserId(), request.getTitle(), request.getContent())
                           .map(PostR2dbcResponse::of);
  }

  @GetMapping("")
  public Mono<ResponseEntity<Flux<PostR2dbcResponse>>> findAllPosts() {
    return postR2dbcService.findAll()
                           .map(PostR2dbcResponse::of)
                           .collectList()
                           .flatMap(posts -> {
                             if (posts.isEmpty()) {
                               return Mono.just(ResponseEntity.notFound().build());
                             }
                             return Mono.just(ResponseEntity.ok(Flux.fromIterable(posts)));
                           });
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<PostR2dbcResponse>> findPostById(@PathVariable("id") Long id) {
    return postR2dbcService.findById(id)
                           .map(p -> ResponseEntity.ok(PostR2dbcResponse.of(p)))
                           .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<PostR2dbcResponse>> updatePost(@PathVariable("id") Long id,
      @RequestBody PostR2dbcUpdateRequest request) {
    return postR2dbcService.update(id, request.getTitle(), request.getContent())
                           .map(p -> ResponseEntity.ok(PostR2dbcResponse.of(p)))
                           .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Object>> deletePost(@PathVariable("id") Long id) {
    return postR2dbcService.findById(id)
                           .flatMap(post -> postR2dbcService.delete(id)
                                                            .then(Mono.just(
                                                                ResponseEntity.noContent()
                                                                              .build())))
                           .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }
}
