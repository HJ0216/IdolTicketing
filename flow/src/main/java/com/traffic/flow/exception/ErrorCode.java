package com.traffic.flow.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
  QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001", "Already Registered in Queue"),
  // 사실상 아래처럼 객체가 생성된 것과 같음
  // private static final ErrorCode QUEUE_ALREADY_REGISTERED_USER =
  //    new ErrorCode(HttpStatus.CONFLICT, "UQ-0001", "Already Registered in Queue");
  QUEUE_ALREADY_REGISTERED_USER_IN_QUEUE(HttpStatus.CONFLICT, "UQ-0002", "Already Registered in Queue-%s");
  // ErrorCode.QUEUE_ALREADY_REGISTERED_USER_IN_QUEUE.build("A");
  // Already Registered in Queue-A
  // 인자를 초과해서 넘길 경우 -> 무시, 인자보다 부족하게 넘김 경우 -> MissingFormatArgumentException 발생

  private final HttpStatus status;
  private final String code;
  private final String reason;

  public ApplicationException build(){
    return new ApplicationException(status, code, reason);
    // status, code, reason은 this.status, this.code, this.reason을 의미
    // ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()를 호출하면 ApplicationException이 생성
  }

  public ApplicationException build(Object ...args){ // 0개 이상의 인자를 받을 수 있음
    return new ApplicationException(status, code, reason.formatted(args));
  }
}
