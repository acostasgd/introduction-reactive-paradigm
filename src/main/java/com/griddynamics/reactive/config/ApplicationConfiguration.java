package com.griddynamics.reactive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public WebClient orderClient(@Value("${reactive.order.info.service.url}") String baseUrl) {
    return WebClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  public WebClient productClient(@Value("${product.info.service.url}") String baseUrl) {
    HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(5));
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl(baseUrl)
        .build();
  }
}
