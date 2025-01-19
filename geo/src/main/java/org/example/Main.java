package org.example;

import java.util.List;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

public class Main {

  public static void main(String[] args) {
    try(var jedispool = new JedisPool("127.0.0.1", 6379)){
      try(Jedis jedis = jedispool.getResource()){
        jedis.geoadd("stores2:geo", 126.98102606983623, 37.57940249726259, "Gyeonghoeru");
        jedis.geoadd("stores2:geo", 126.96865587536988, 37.570777342456765, "Gyeonghuigung Palace");

        Double geodist = jedis.geodist("stores2:geo", "Gyeonghoeru", "Gyeonghuigung Palace");
        System.out.println("geodist = " + geodist);

//        List<GeoRadiusResponse> geoSearch = jedis.geosearch(
//            "stores2:geo",
//            new GeoCoordinate(126.974, 37.575),
//            1500,
//            GeoUnit.M);
//        geoSearch.forEach(r -> System.out.println("%s %f %f".formatted(r.getMemberByString(), r.getCoordinate().getLongitude(), r.getCoordinate().getLatitude())));
        // GeoSearch는 기본적으로 멤버만 반환
        // Jedis에서 WITHCOORD가 활성화되지 않았다면 getCoordinate()가 null을 반환

        List<GeoRadiusResponse> geoSearch = jedis.geosearch("stores2:geo",
            new GeoSearchParam()
                .fromLonLat(new GeoCoordinate(126.974, 37.575))
                .byRadius(1500, GeoUnit.M)
                .withCoord()
        );

        geoSearch.forEach(r ->
            System.out.println("%s %f %f".formatted(
                r.getMemberByString(),
                r.getCoordinate().getLongitude(),
                r.getCoordinate().getLatitude())
            )
        );

        jedis.unlink("stores2:geo");
      }
    }
  }
}