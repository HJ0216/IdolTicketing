package com.example.webflux1.client;

import com.example.webflux1.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostClient {
  private final WebClient webClient;
  private final String baseUrl = "http://localhost:8090/";

  // WebClient -> mvc("/posts/{id}")
  public Mono<PostResponse> getPost(Long id) {
    String uriString = UriComponentsBuilder.fromHttpUrl(baseUrl)
                                           .path("/posts/%d".formatted(id))
                                           .buildAndExpand()
                                           .toUriString();

    return webClient.get()
                    .uri(uriString)
                    .retrieve()
                    .bodyToMono(PostResponse.class);
  }
}
