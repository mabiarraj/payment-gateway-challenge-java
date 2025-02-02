package com.checkout.payment.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BankProcessorResponse {
  private Boolean authorized;
  private String authorization_code;
}
