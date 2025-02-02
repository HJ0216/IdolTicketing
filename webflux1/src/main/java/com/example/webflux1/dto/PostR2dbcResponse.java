package com.example.webflux1.dto;

import com.example.webflux1.repository.Post;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostR2dbcResponse {
  private Long id;
  private Long userId;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static PostR2dbcResponse of(Post post) {
    return PostR2dbcResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .build();
  }
}
