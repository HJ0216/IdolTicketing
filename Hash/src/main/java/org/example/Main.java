package org.example;

import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {

  public static void main(String[] args) {
    try(var redisPool = new JedisPool("127.0.0.1", 6379)){
      try(Jedis jedis = redisPool.getResource()){
        // Hash Set
        jedis.hset("users:2:info", "name", "grep2");

        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("email", "grep2@email.com");
        userInfo.put("phone", "010-5678-5678");

        jedis.hset("users:2:info", userInfo);

        // Delete
        jedis.hdel("users:2:info", "phone");

        // Get
        System.out.println(jedis.hget("users:2:info", "email"));

        Map<String, String> user2Info = jedis.hgetAll("users:2:info");
        user2Info.forEach((k,v) -> System.out.printf("%s %s%n", k, v));

        // Increment
        jedis.hincrBy("users:2:info", "visits", 10);
      }
    }
  }
}