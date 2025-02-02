package com.example.webflux1.service;

import com.example.webflux1.repository.Post;
import com.example.webflux1.repository.PostR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PostR2dbcService {
  private final PostR2dbcRepository postR2dbcRepository;

  // create
  public Mono<Post> create(Long userId, String title, String content) {
    return postR2dbcRepository.save(Post.builder()
                                        .userId(userId)
                                        .title(title)
                                        .content(content)
                                        .build());
  }

  // read
  public Flux<Post> findAll() {
    return postR2dbcRepository.findAll();
  }

  public Mono<Post> findById(Long id) {
    return postR2dbcRepository.findById(id);
  }

  public Flux<Post> findAllByUserId(Long userId) {
    return postR2dbcRepository.findAllByUserId(userId);
  }

  // update
  public Mono<Post> update(Long id, String title, String content) {
    return postR2dbcRepository.findById(id)
        .flatMap(p -> {
          p.setTitle(title);
          p.setContent(content);
          return postR2dbcRepository.save(p);
        });
  }

  // delete
  public Mono<Void> delete(Long id) {
    return postR2dbcRepository.deleteById(id);
  }

}
