package com.example.spring_boot_cache.domain.repository;

import com.example.spring_boot_cache.domain.entity.RedisHashUser;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashUserRepository extends CrudRepository<RedisHashUser, Long> {

}
