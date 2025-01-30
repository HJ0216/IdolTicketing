package org.example;

public class Main {

  public static void main(String[] args) {
    Publisher publisher = new Publisher();
//    publisher.startFlux()
//        .subscribe(System.out::println);
    // SUB: subscribe → PUB: onSubscribe → SUB: request/cancel → PUB: onNext → PUB: onComplete/onError

//    publisher.startMono()
//        .subscribe();

    publisher.startMonoEmpty()
             .subscribe();
    // onNext가 호출되지 않음 → 데이터를 보내지 않음

  }
}