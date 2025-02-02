package com.example.webflux1.config;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableR2dbcRepositories
// 리액티브 리포지토리(ReactiveCrudRepository 등)를 자동으로 스캔하고, R2DBC를 통해 사용할 수 있도록 설정
@EnableR2dbcAuditing
// R2DBC 엔터티의 자동 감사(Auditing) 기능을 활성화, createdAt, updatedAt 같은 값을 자동으로 입력
public class R2dbcConfig implements ApplicationListener<ApplicationReadyEvent> {

  private final DatabaseClient databaseClient;
  // MySQL 연결 시, 객체 생성은 하지만 Connection까지는 보장하지 않음
  // application.properties 파일에서 비밀번호 틀려도 Spring 연결 O
  // 실행 단계에서 Validation 추가

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    databaseClient.sql("SELECT 1").fetch().one()
                  .subscribe(
                      success -> {
                        log.info("Initialize r2dbc database connection");
                      },
                      error -> {
                        log.error("Error initializing r2dbc database connection");
                        CompletableFuture.runAsync(
                            () -> SpringApplication.exit(event.getApplicationContext(), () -> 1));
                      }
                      // java.lang.IllegalStateException: block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-tcp-nio-3
                      // SpringApplication.exit() 호출이 블로킹 문제를 유발
                      // CompletableFuture.runAsync()를 사용 -> Spring의 종료 로직이 비동기적으로 동작하도록 변경
                      // Connection has been closed by peer이 반복적으로 나타나는 이유
                      // 애플리케이션이 완전히 종료되기 전에 커넥션 풀에서 재연결을 계속 시도
                  );
  }
}
