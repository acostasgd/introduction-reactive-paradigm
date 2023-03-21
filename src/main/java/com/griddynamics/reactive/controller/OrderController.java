package com.griddynamics.reactive.controller;

import com.griddynamics.reactive.model.Order;
import com.griddynamics.reactive.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

  private final OrderService orderService;

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Order> userOrders(@PathVariable("id") String id) {
    return orderService.getUserOrders(id);
  }
}
