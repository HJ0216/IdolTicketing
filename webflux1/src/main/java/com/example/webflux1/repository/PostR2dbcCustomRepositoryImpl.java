package com.example.webflux1.repository;

import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class PostR2dbcCustomRepositoryImpl implements PostR2dbcCustomRepository {
  private final DatabaseClient databaseClient;

  @Override
  public Flux<Post> findAllByUserId(Long userId) {
    String sql = """
                SELECT p.id as pid, p.user_id as userId, p.title, p.content, p.created_at as pcreatedAt, p.updated_at as pupdatedAt,
                       u.id as uid, u.name, u.email, u.created_at as ucreatedAt, u.updated_at as uupdatedAt
                FROM posts p
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.user_id = :userId
        """;

    return databaseClient.sql(sql)
                         .bind("userId", userId)
                         // :userId에 매핑 -> 오류나는 부분 IntelliJ 문제
                         .fetch()
                         // fetch().all()을 쓰면 Flux, fetch().one()을 쓰면 Mono를 반환
                         .all()
                         .map(row -> Post.builder()
                                         .id((Long) row.get("pid"))
                                         .userId((Long) row.get("userId"))
                                         .title((String) row.get("title"))
                                         .content((String) row.get("content"))
                                         .user(User.builder()
                                                   .id((Long) row.get("uid"))
                                                   .name((String) row.get("name"))
                                                   .email((String) row.get("email"))
                                                   .createdAt(((ZonedDateTime) row.get("ucreatedAt")).toLocalDateTime())
                                                   .updatedAt(((ZonedDateTime) row.get("uupdatedAt")).toLocalDateTime())
                                                   .build())
                                         .createdAt(((ZonedDateTime) row.get("pcreatedAt")).toLocalDateTime())
                                         .updatedAt(((ZonedDateTime) row.get("pupdatedAt")).toLocalDateTime())
                                         .build());
  }
}
