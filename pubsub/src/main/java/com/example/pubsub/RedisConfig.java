package com.example.pubsub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

  @Bean
  MessageListenerAdapter messageListenerAdapter() {
    return new MessageListenerAdapter(new MessageListenerService());
    // MessageListenerAdapter: 메시지를 처리할 서비스 클래스(MessageListenerService)를 어댑터로 감싸서 Redis가 이해할 수 있는 형태로 변환
  }

  @Bean
  RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, MessageListenerAdapter listener) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listener, ChannelTopic.of("users:unregister"));
    // RedisMessageListenerContainer: Redis와 연결해서 메시지를 듣는 컨테이너
    // setConnectionFactory: Redis 연결 설정
    // addMessageListener: 메시지 리스너를 특정 채널(users:unregister)에 바인딩
    // Redis와 연결하고, 'users:unregister' 채널에서 메시지가 오면 들을 준비

    return container;
  }
}
