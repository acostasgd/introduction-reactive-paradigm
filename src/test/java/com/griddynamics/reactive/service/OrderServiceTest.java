package com.griddynamics.reactive.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.griddynamics.reactive.client.OrderInfoClient;
import com.griddynamics.reactive.client.ProductInfoClient;
import com.griddynamics.reactive.document.User;
import com.griddynamics.reactive.model.OrderInfo;
import com.griddynamics.reactive.model.ProductInfo;
import com.griddynamics.reactive.repository.UserInfoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

  private static final String USER_ID = "04bc46d2c332";
  private static final String PHONE_NUMBER = "02339443403";
  private static final User USER = new User(USER_ID, "username", PHONE_NUMBER);

  @Mock private OrderInfoClient orderInfoClient;
  @Mock private ProductInfoClient productInfoClient;
  @Mock private UserInfoRepository repository;
  @InjectMocks private OrderService orderService;

  @Test
  public void getUserOrdersForMissingUserTest() {

    // Given
    when(repository.findById(USER_ID)).thenReturn(Mono.empty());

    // When
    var orders = orderService.getUserOrders(USER_ID);

    // Then
    verifyNoInteractions(orderInfoClient);
    verifyNoInteractions(productInfoClient);
    StepVerifier.create(orders).expectComplete().verify();
  }

  @Test
  public void getUserOrdersForMissingOrdersTest() {

    // Given
    when(repository.findById(USER_ID)).thenReturn(Mono.just(USER));
    when(orderInfoClient.getOrders(PHONE_NUMBER)).thenReturn(Flux.empty());

    // When
    var orders = orderService.getUserOrders(USER_ID);

    // Then
    verifyNoInteractions(productInfoClient);
    StepVerifier.create(orders).expectComplete().verify();
  }

  @Test
  public void getUserOrdersForMissingOrderProductsTest() {

    // Given
    var validProductCode = "029554746";
    var failProductCode = "029987878";
    var firstOrder = new OrderInfo();
    firstOrder.setPhoneNumber(PHONE_NUMBER);
    firstOrder.setOrderNumber("001");
    firstOrder.setProductCode(validProductCode);
    var secondOrder = new OrderInfo();
    secondOrder.setPhoneNumber(PHONE_NUMBER);
    secondOrder.setOrderNumber("002");
    secondOrder.setProductCode(failProductCode);
    var highScoreProduct = new ProductInfo();
    highScoreProduct.setProductCode(validProductCode);
    highScoreProduct.setProductId("0223993023");
    highScoreProduct.setProductName("first product");
    highScoreProduct.setScore(12D);
    var lowScoreProduct = new ProductInfo();
    lowScoreProduct.setProductCode(validProductCode);
    lowScoreProduct.setProductId("024993493");
    lowScoreProduct.setProductName("second product");
    lowScoreProduct.setScore(8D);
    when(repository.findById(USER_ID)).thenReturn(Mono.just(USER));
    when(orderInfoClient.getOrders(PHONE_NUMBER))
        .thenReturn(Flux.fromIterable(List.of(firstOrder, secondOrder)));
    when(productInfoClient.getProducts(validProductCode))
        .thenReturn(Flux.fromIterable(List.of(highScoreProduct, lowScoreProduct)));
    when(productInfoClient.getProducts(failProductCode))
        .thenReturn(Flux.error(new RuntimeException("Fails to retrieve products")));

    // When
    var orders = orderService.getUserOrders(USER_ID);

    // Then
    orders.subscribe(System.out::println);
    StepVerifier.create(orders)
        .assertNext(
            order -> {
              assertThat(order.getOrderNumber()).isEqualTo("001");
              assertThat(order.getUserName()).isEqualTo("username");
              assertThat(order.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
              assertThat(order.getProductCode()).isEqualTo(validProductCode);
              assertThat(order.getProductName()).isEqualTo("first product");
              assertThat(order.getProductId()).isEqualTo("0223993023");
            })
        .assertNext(
            order -> {
              assertThat(order.getOrderNumber()).isEqualTo("002");
              assertThat(order.getUserName()).isEqualTo("username");
              assertThat(order.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
              assertThat(order.getProductCode()).isEqualTo(failProductCode);
              assertThat(order.getProductName()).isNull();
              assertThat(order.getProductId()).isNull();
            })
        .expectComplete()
        .verify();
  }

  @Test
  public void getUserOrdersForOrderProductsTest() {

    // Given
    var productCode = "029554746";
    var orderInfo = new OrderInfo();
    orderInfo.setPhoneNumber(PHONE_NUMBER);
    orderInfo.setOrderNumber("001");
    orderInfo.setProductCode(productCode);
    var highScoreProduct = new ProductInfo();
    highScoreProduct.setProductCode(productCode);
    highScoreProduct.setProductId("0223993023");
    highScoreProduct.setProductName("first product");
    highScoreProduct.setScore(8.2);
    var lowScoreProduct = new ProductInfo();
    lowScoreProduct.setProductCode(productCode);
    lowScoreProduct.setProductId("024993493");
    lowScoreProduct.setProductName("second product");
    lowScoreProduct.setScore(8.1);
    when(repository.findById(USER_ID)).thenReturn(Mono.just(USER));
    when(orderInfoClient.getOrders(PHONE_NUMBER)).thenReturn(Flux.fromIterable(List.of(orderInfo)));
    when(productInfoClient.getProducts(productCode))
        .thenReturn(Flux.fromIterable(List.of(highScoreProduct, lowScoreProduct)));

    // When
    var orders = orderService.getUserOrders(USER_ID);

    // Then
    orders.subscribe(System.out::println);
    StepVerifier.create(orders)
        .assertNext(
            order -> {
              assertThat(order.getOrderNumber()).isEqualTo("001");
              assertThat(order.getUserName()).isEqualTo("username");
              assertThat(order.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
              assertThat(order.getProductCode()).isEqualTo(productCode);
              assertThat(order.getProductName()).isEqualTo("first product");
              assertThat(order.getProductId()).isEqualTo("0223993023");
            })
        .expectComplete()
        .verify();
  }
}
