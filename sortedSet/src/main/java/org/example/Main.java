package org.example;

import java.util.HashMap;
import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

public class Main {

  public static void main(String[] args) {
    try(var jedispool = new JedisPool("127.0.0.1", 6379)){
      try(Jedis jedis = jedispool.getResource()) {
        // Sorted set
        HashMap<String, Double> scores = new HashMap<>();
        scores.put("user1", 100.0);
        scores.put("user2", 10.0);
        scores.put("user3", 20.0);
        scores.put("user4", 30.0);
        scores.put("user5", 40.0);

        jedis.zadd("game2:scores", scores);

        List<String> zrange = jedis.zrange("game2:scores", 0, Long.MAX_VALUE);
        zrange.forEach(System.out::println);

        List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
        tuples.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));

        System.out.println(jedis.zcard("game2:scores"));

        jedis.zincrby("game2:scores", 50, "user2");

        List<Tuple> tuples2 = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
        tuples2.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));
      }
    }
  }
}