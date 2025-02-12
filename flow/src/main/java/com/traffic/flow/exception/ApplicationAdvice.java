package com.traffic.flow.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
// Spring에서 전역 예외 처리를 담당하는 클래스
// 컨트롤러에서 발생하는 예외를 가로채고, 적절한 응답을 반환
public class ApplicationAdvice {

  @ExceptionHandler(ApplicationException.class)
  // ApplicationException이 발생했을 때 실행되는 핸들러
  Mono<ResponseEntity<ServerExceptionResponse>> applicationExceptionHandler(ApplicationException e) {
    return Mono.just(ResponseEntity // ResponseEntity: HTTP 응답을 생성하는 객체
               .status(e.getStatus())
               .body(new ServerExceptionResponse(e.getCode(), e.getReason()))); // JSON 응답으로 변환할 데이터 구조
  }

  public record ServerExceptionResponse(String code, String reason) {

  }
}
