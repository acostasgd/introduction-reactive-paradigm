package com.griddynamics.reactive.client;

import com.griddynamics.reactive.model.ProductInfo;
import com.griddynamics.reactive.utils.LoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductInfoClient {

  private final WebClient productClient;

  public Flux<ProductInfo> getProducts(String productCode) {
    return productClient
        .get()
        .uri(
            builder ->
                builder
                    .path("/productInfoService/product/names")
                    .queryParam("productCode", productCode)
                    .build())
        .retrieve()
        .bodyToFlux(ProductInfo.class)
        .doOnEach(
            LoggerUtils.logOnError(
                error -> log.error("Error occurred while call product info service", error)))
        .doOnEach(
            LoggerUtils.logOnNext(
                productInfo -> log.info("ProductInfoService: Product info {}", productInfo)));
  }
}
