package com.example.webflux1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostR2dbcUpdateRequest {
  private String title;
  private String content;

}
