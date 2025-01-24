package com.example.spring_boot_cache.controller;

import com.example.spring_boot_cache.domain.entity.RedisHashUser;
import com.example.spring_boot_cache.domain.entity.User;
import com.example.spring_boot_cache.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

  private final UserService userService;

  @GetMapping("/users/{id}")
  public User getUser(@PathVariable Long id) {
    return userService.getUser(id);
  }

  @GetMapping("/redishash-users/{id}")
  public RedisHashUser getUser2(@PathVariable Long id) {
    return userService.getUser2(id);
  }

  @GetMapping("/cacheable-users/{id}")
  public User getUser3(@PathVariable Long id) {
    return userService.getUser3(id);
  }

}
