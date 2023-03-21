package com.griddynamics.reactive.client;

import com.griddynamics.reactive.model.OrderInfo;
import com.griddynamics.reactive.utils.LoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInfoClient {

  private final WebClient orderClient;

  public Flux<OrderInfo> getOrders(String phoneNumber) {
    return orderClient
        .get()
        .uri(
            builder ->
                builder
                    .path("/orderSearchService/order/phone")
                    .queryParam("phoneNumber", phoneNumber)
                    .build())
        .retrieve()
        .bodyToFlux(OrderInfo.class)
        .doOnEach(
            LoggerUtils.logOnError(
                error -> log.error("Error occurred while call order info service", error)))
        .doOnEach(
            LoggerUtils.logOnNext(
                orderInfo -> log.info("OrderSearchService: Order info {}", orderInfo)));
  }
}
