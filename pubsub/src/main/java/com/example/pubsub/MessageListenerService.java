package com.example.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageListenerService implements MessageListener {
  // Redis에서 메시지가 오면 실행될 리스너 역할

  @Override
  public void onMessage(Message message, byte[] pattern) {
    // Redis에서 메시지가 오면 이 메서드가 호출

    log.info("Received {} channel: {}", new String(message.getChannel()), new String(message.getBody()));
  }
}
