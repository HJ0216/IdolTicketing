package com.example.spring_boot_cache.domain.service;

import static com.example.spring_boot_cache.config.CacheConfig.CACHE1;

import com.example.spring_boot_cache.domain.entity.RedisHashUser;
import com.example.spring_boot_cache.domain.entity.User;
import com.example.spring_boot_cache.domain.repository.RedisHashUserRepository;
import com.example.spring_boot_cache.domain.repository.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final RedisHashUserRepository redisHashUserRepository;
  private final UserRepository userRepository;
  private final RedisTemplate<String, User> userRedisTemplate;
  private final RedisTemplate<String, Object> objectRedisTemplate;

  public User getUser(final Long id) {
    String key = "users:%d".formatted(id);

    // 1. cache get
//    User cachedUser = userRedisTemplate.opsForValue().get(key);
    Object cachedUser = objectRedisTemplate.opsForValue().get(key);
    if (cachedUser != null) {
      return (User)cachedUser;
    }

    // 2. else db
    User user = userRepository.findById(id).orElseThrow();
//    userRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(30));
    objectRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(30));
    return user;
  }

  public RedisHashUser getUser2(final Long id) {

    // 1. cache get
    RedisHashUser cachedUser = redisHashUserRepository.findById(id).orElseGet(() -> {
      // 2. else db
      User findUser = userRepository.findById(id).orElseThrow();

      return redisHashUserRepository.save(RedisHashUser.builder()
                                                       .id(findUser.getId())
                                                       .email(findUser.getEmail())
                                                       .name(findUser.getName())
                                                       .createdAt(findUser.getCreatedAt())
                                                       .updatedAt(findUser.getUpdatedAt())
                                                       .build());
    });

    return cachedUser;
  }

  @Cacheable(cacheNames = CACHE1, key = "'users:' + #id")
  public User getUser3(final Long id) {
    return userRepository.findById(id).orElseThrow();
  }
}
