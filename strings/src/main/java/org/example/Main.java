package org.example;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class Main {

  public static void main(String[] args) {
    try(var jedispool = new JedisPool("127.0.0.1", 6379)){
      try(Jedis jedis = jedispool.getResource()){
//        jedis.set("users:300:name", "imjedis");
//        jedis.set("users:300:email", "imjedis@email.com");
//        jedis.set("users:300:age", "30");
//
//        String userName = jedis.get("users:300:name");
//        System.out.println("userName = " + userName);
//
//        List<String> userInfo = jedis.mget("users:300:name", "users:300:email", "users:300:age");
//        userInfo.forEach(System.out::println);
//
//        long counter1 = jedis.incr("counter1");
//        System.out.println("counter1 = " + counter1);
//
//        counter1 = jedis.incrBy("counter1", 10L);
//        System.out.println("counter1 = " + counter1);
//
//        counter1 = jedis.decr("counter1");
//        System.out.println("counter1 = " + counter1);
//
//        counter1 = jedis.decrBy("counter1", 10L);
//        System.out.println("counter1 = " + counter1);

        // Redis pipelining
        // 여러 명령을 한 번에 서버에 보내서 처리하고 결과를 받는 방식
        // 네트워크 왕복(Round Trip) 시간을 줄여 성능을 향상시킬 수 있음

        Pipeline pipelined = jedis.pipelined();
        pipelined.set("users:400:name", "jedisPipe");
        pipelined.set("users:400:email", "jedisPipe@email.com");
        pipelined.set("users:400:age", "25");

        List<Object> objects = pipelined.syncAndReturnAll();
        objects.forEach(i -> System.out.println(i.toString()));

      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}