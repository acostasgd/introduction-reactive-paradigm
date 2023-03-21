package com.griddynamics.reactive.service;

import com.griddynamics.reactive.client.OrderInfoClient;
import com.griddynamics.reactive.client.ProductInfoClient;
import com.griddynamics.reactive.document.User;
import com.griddynamics.reactive.model.Order;
import com.griddynamics.reactive.model.OrderInfo;
import com.griddynamics.reactive.model.ProductInfo;
import com.griddynamics.reactive.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderInfoClient orderInfoClient;
  private final ProductInfoClient productInfoClient;
  private final UserInfoRepository repository;

  public Flux<Order> getUserOrders(String id) {
    return repository
        .findById(id)
        .flatMapMany(
            user ->
                orderInfoClient
                    .getOrders(user.getPhone())
                    .flatMap(
                        orderInfo ->
                            getRelevantProduct(orderInfo.getProductCode())
                                .map(productInfo -> buildOrder(user, orderInfo, productInfo))));
  }

  private Mono<Optional<ProductInfo>> getRelevantProduct(String productCode) {
    return productInfoClient
        .getProducts(productCode)
        .collectList()
        .onErrorReturn(List.of())
        .map(products -> products.stream().max(Comparator.comparing(ProductInfo::getScore)));
  }

  private Order buildOrder(User user, OrderInfo order, Optional<ProductInfo> productInfo) {
    var builder = Order.builder();
    builder
        .orderNumber(order.getOrderNumber())
        .userName(user.getName())
        .phoneNumber(user.getPhone())
        .productCode(order.getProductCode());
    productInfo.ifPresent(
        info -> builder.productName(info.getProductName()).productId(info.getProductId()));
    return builder.build();
  }
}
