package com.example.spring_boot_cache.domain.repository;

import com.example.spring_boot_cache.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
