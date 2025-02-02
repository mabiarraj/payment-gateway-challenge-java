package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PaymentResponseTransformer {
  public PostPaymentResponse requestToResponse(PostPaymentRequest postPaymentRequest, PaymentStatus status) {
    return PostPaymentResponse.builder()
        .id(UUID.randomUUID())
        .status(status)
        .amount(postPaymentRequest.getAmount())
        .expiryYear(postPaymentRequest.getExpiryYear())
        .expiryMonth(postPaymentRequest.getExpiryMonth())
        .build();
  }

}
