package com.griddynamics.reactive.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.griddynamics.reactive.model.Order;
import com.griddynamics.reactive.service.OrderService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@SpringBootTest
@AutoConfigureWebTestClient
public class OrderControllerTest {

  private static final String USER_ID = "ac5ec2c977f04c8d";
  private static final String PATH = "/api/v1/order/{id}";

  @MockBean private OrderService orderService;
  @Autowired private WebTestClient webClient;

  @Test
  public void userOrdersForUnexpectedErrorTest() {

    // Given
    when(orderService.getUserOrders(USER_ID))
        .thenReturn(Flux.error(new RuntimeException("Unexpected error occurred")));

    // When
    var orderResponse =
        webClient.get().uri(PATH, USER_ID).exchange().expectBodyList(Order.class).returnResult();

    // Then
    orderResponse.getStatus().is5xxServerError();
  }

  @Test
  public void userOrdersTest() {

    // Given
    var userName = "user name";
    var phoneNumber = "02993304";
    var firstOrder =
        Order.builder()
            .orderNumber("02339343")
            .userName(userName)
            .phoneNumber(phoneNumber)
            .productCode("01100223")
            .productName("first product name")
            .productId("01122203034")
            .build();
    var secondOrder =
        Order.builder()
            .orderNumber("0329923")
            .userName(userName)
            .phoneNumber(phoneNumber)
            .productCode("059454954")
            .productName("second product name")
            .productId("0998239289")
            .build();
    when(orderService.getUserOrders(USER_ID))
        .thenReturn(Flux.fromIterable(List.of(firstOrder, secondOrder)));

    // When
    var orderResponse =
        webClient.get().uri(PATH, USER_ID).exchange().expectBodyList(Order.class).returnResult();

    // Then
    orderResponse.getStatus().is2xxSuccessful();
    var orders = orderResponse.getResponseBody();
    assertThat(orders).isNotNull();
    assertThat(Set.copyOf(orders)).isEqualTo(Set.of(firstOrder, secondOrder));
  }
}
