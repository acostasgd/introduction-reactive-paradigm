package com.griddynamics.reactive.config;

import com.griddynamics.reactive.utils.LoggerUtils;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {

  private static final String REQUEST_HEADER = "requestId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var headers = exchange.getRequest().getHeaders();
    String requestId = "";
    if (headers.containsKey(REQUEST_HEADER)) {
      requestId = Objects.requireNonNull(headers.get(REQUEST_HEADER)).get(0);
    }
    var context = Context.of(LoggerUtils.LOG_CONTEXT, requestId);
    return chain.filter(exchange).contextWrite(context);
  }
}
