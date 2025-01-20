package org.example;

import java.util.stream.IntStream;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class Main {

  public static void main(String[] args) {
    try(var jedispool = new JedisPool("127.0.0.1", 6379)) {
      try (Jedis jedis = jedispool.getResource()) {
//        jedis.setbit("request-somepage-20250107", 100, true);
//        jedis.setbit("request-somepage-20250107", 110, true);
//        jedis.setbit("request-somepage-20250107", 120, true);

//        System.out.println(jedis.getbit("request-somepage-20250107", 100));
//        System.out.println(jedis.getbit("request-somepage-20250107", 50));

//        System.out.println(jedis.bitcount("request-somepage-20250107"));

        // bitmap vs set
        Pipeline pipelined = jedis.pipelined();
        IntStream.rangeClosed(0, 100000).forEach(i -> {
          pipelined.sadd("request-somepage-set-20250106", String.valueOf(i), "1");
          pipelined.setbit("request-somepage-bit-20250106", i, true);

          if(i == 1000){
            pipelined.sync();
          }

          pipelined.sync();
        });
      }
    }
  }
}