package com.traffic.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowApplication.class, args);
	}

/*
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		reactiveRedisTemplate.opsForValue().set("testKey", "testValue").subscribe();
		// ReactiveRedisTemplate.opsForValue().set("testKey", "testValue"): Mono<Boolean> 반환
		// Reactor의 Mono나 Flux는 구독(subscribe)하기 전까지 실행되지 않음
		// 리액티브 스트림에서는 구독자가 있어야 데이터 흐름이 시작됨
	}
*/
}
