package com.example.webflux1.dto;

import com.example.webflux1.repository.Post;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPostResponse {
  private Long id;
  private String userName;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UserPostResponse of(Post post) {
    return UserPostResponse.builder()
                            .id(post.getId())
                            .userName("1000L")
                            .title(post.getTitle())
                            .content(post.getContent())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .build();
  }

}
