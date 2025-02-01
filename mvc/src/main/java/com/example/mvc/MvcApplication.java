package com.example.mvc;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class MvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcApplication.class, args);
	}

	@GetMapping("/posts/{id}")
	public Map<String, String> getPost(@PathVariable Long id) throws Exception {
		Thread.sleep(300);
		// 동기: 3초, 요청건수 3개 -> 총 9초
		// 비동기: 3초, 요청건수 3개 -> 총 9초 미만

		if(id > 10L){
			throw new Exception("Invalid post id");
		}

		return Map.of("id", id.toString(), "content", "Post content id is %d".formatted(id));
	}

}
