package com.traffic.website;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootApplication
@Controller
public class WebsiteApplication {
	RestTemplate restTemplate = new RestTemplate();
	// Spring 애플리케이션이 외부 API와 통신할 때 요청(Request)을 보내고, 응답(Response)을 받을 수 있도록 해주는 클래스

	public static void main(String[] args) {
		SpringApplication.run(WebsiteApplication.class, args);
	}

	@GetMapping("/")
	public String index(@RequestParam(name = "queue", defaultValue = "default") String queue,
			@RequestParam(name = "user_id") Long userId,
			HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();
		String cookieName = "user-queue-%s-token".formatted(queue);
		// 쿠키는 포트를 제외한 도메인 기반으로 동작
		// 포트가 다르더라도 도메인이 같으면 쿠기 공유 가능

		String token = "";
		if(cookies != null) {
			Optional<Cookie> findCookie = Arrays.stream(cookies).filter(
					cookie -> cookie.getName().equalsIgnoreCase(cookieName)).findFirst();
			token = findCookie.orElse(new Cookie(cookieName, "")).getValue();
		}

		URI uri = UriComponentsBuilder.fromUriString("http://127.0.0.1:9010")
																	.path("/api/v1/queue/allowed")
																	.queryParam("queue", queue)
																	.queryParam("user_id", userId)
																	.encode() // 사용자가 인지 가능한 데이터를 컴퓨터가 인지 가능한 데이터로 변경하는 것
																	.build()
																	.toUri();

		ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri,
				AllowedUserResponse.class);
		if(response.getBody() == null || !response.getBody().allowed()){
			// 대기 페이지로 리다이렉트
			return "redirect:http://127.0.0.1:9010/waiting-room?user_id=%d&redirect_url=%s".formatted(
					userId, "http://127.0.0.1:9000?user_id=%d".formatted(userId));
			// redirect:URL Spring MVC에서 특정 URL로 리다이렉트하는 방식
		}

		return "index";
	}

	public record AllowedUserResponse(Boolean allowed){

	}

}
