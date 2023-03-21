package com.griddynamics.reactive.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.griddynamics.reactive.model.OrderInfo;
import java.util.List;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

@SpringBootTest
public class OrderInfoClientTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String SERVICE_PATH = "/orderSearchService/order/phone";
  private static final String QUERY_PARAM = "phoneNumber";
  private static final String PHONE_NUMBER = "070234098";

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("reactive.order.info.service.url", wireMockServer::baseUrl);
  }

  @RegisterExtension
  static WireMockExtension wireMockServer =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Autowired private OrderInfoClient orderInfoClient;

  @Test
  public void getOrdersForErrorResponseTest() {

    // Given
    wireMockServer.stubFor(
        get(urlPathEqualTo(SERVICE_PATH))
            .withQueryParam(QUERY_PARAM, equalTo(PHONE_NUMBER))
            .willReturn(aResponse().withStatus(400)));

    // When
    var orders = orderInfoClient.getOrders(PHONE_NUMBER);

    // Then
    StepVerifier.create(orders).expectError().verify();
  }

  @Test
  public void getOrdersForSuccessResponseTest() throws JsonProcessingException {

    // Given
    var firstOrder = new OrderInfo();
    firstOrder.setPhoneNumber(PHONE_NUMBER);
    firstOrder.setOrderNumber("001");
    firstOrder.setProductCode("029554746");
    var secondOrder = new OrderInfo();
    secondOrder.setPhoneNumber(PHONE_NUMBER);
    secondOrder.setOrderNumber("002");
    secondOrder.setProductCode("029987878");
    wireMockServer.stubFor(
        get(urlPathEqualTo(SERVICE_PATH))
            .withQueryParam(QUERY_PARAM, equalTo(PHONE_NUMBER))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .withBody(OBJECT_MAPPER.writeValueAsBytes(List.of(firstOrder, secondOrder)))));

    // When
    var orders = orderInfoClient.getOrders(PHONE_NUMBER);

    // Then
    StepVerifier.create(orders)
        .expectNext(firstOrder)
        .expectNext(secondOrder)
        .expectComplete()
        .verify();
  }
}
