package com.griddynamics.reactive.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {

  private String orderNumber;
  private String userName;
  private String phoneNumber;
  private String productCode;
  private String productName;
  private String productId;
}
