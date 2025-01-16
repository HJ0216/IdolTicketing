package org.example;

import java.util.List;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {

  public static void main(String[] args) {
    try(var jedisPool = new JedisPool("127.0.0.1", 6379)){
      try(Jedis jedis = jedisPool.getResource()){
        // 1. stack
//        jedis.rpush("stack2", "aaa");
//        jedis.rpush("stack2", "bbb");
//        jedis.rpush("stack2", "ccc");
//
//        List<String> stack2 = jedis.lrange("stack2", 0, -1);
//        stack2.forEach(System.out::println);
//
//        jedis.rpop("stack2");

        // 2. queue
//        jedis.rpush("queue2", "111");
//        jedis.rpush("queue2", "222");
//        jedis.rpush("queue2", "333");
//
//        System.out.println(jedis.lpop("queue2"));
//        System.out.println(jedis.lpop("queue2"));
//        System.out.println(jedis.lpop("queue2"));

        // 3. block brpop, blpop
//        List<String> blpop = jedis.blpop(10, "queue:blocking");
//        if (blpop != null) {
//          blpop.forEach(System.out::println);
//        }

        // 4. set
        jedis.sadd("users:300:follow", "100", "200", "300");
        jedis.srem("users:300:follow", "100");

        Set<String> smembers = jedis.smembers("users:300:follow");
        smembers.forEach(System.out::println);

        System.out.println(jedis.sismember("users:300:follow", "200"));
        System.out.println(jedis.sismember("users:300:follow", "250"));

        Set<String> sinter = jedis.sinter("users:200:follow", "users:300:follow");
        sinter.forEach(System.out::println);
      }
    }
  }
}