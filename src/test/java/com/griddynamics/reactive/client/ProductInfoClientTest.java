package com.griddynamics.reactive.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.griddynamics.reactive.model.ProductInfo;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
public class ProductInfoClientTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String SERVICE_PATH = "/productInfoService/product/names";
  private static final String QUERY_PARAM = "productCode";
  private static final String PRODUCT_CODE = "029554746";

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("product.info.service.url", wireMockServer::baseUrl);
  }

  @RegisterExtension
  static WireMockExtension wireMockServer =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Autowired private ProductInfoClient productInfoClient;

  @Test
  public void getProductsForErrorResponseTest() {

    // Given
    wireMockServer.stubFor(
        get(urlPathEqualTo(SERVICE_PATH))
            .withQueryParam(QUERY_PARAM, equalTo(PRODUCT_CODE))
            .willReturn(aResponse().withStatus(400)));

    // When
    var products = productInfoClient.getProducts(PRODUCT_CODE);

    // Then
    StepVerifier.create(products).expectError().verify();
  }

  @Test
  public void getProductsForSuccessResponseTest() throws JsonProcessingException {

    // Given
    var firstProduct = new ProductInfo();
    firstProduct.setProductCode(PRODUCT_CODE);
    firstProduct.setProductId("0223993023");
    firstProduct.setProductName("first product");
    var secondProduct = new ProductInfo();
    secondProduct.setProductCode(PRODUCT_CODE);
    secondProduct.setProductId("024993493");
    secondProduct.setProductName("second product");
    wireMockServer.stubFor(
        get(urlPathEqualTo(SERVICE_PATH))
            .withQueryParam(QUERY_PARAM, equalTo(PRODUCT_CODE))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .withBody(
                        OBJECT_MAPPER.writeValueAsBytes(List.of(firstProduct, secondProduct)))));

    // When
    var products = productInfoClient.getProducts(PRODUCT_CODE);

    // Then
    StepVerifier.create(products)
        .expectNext(firstProduct)
        .expectNext(secondProduct)
        .expectComplete()
        .verify();
  }
}
