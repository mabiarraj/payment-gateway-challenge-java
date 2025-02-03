package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PaymentResponseTransformer {
  public PostPaymentResponse requestToResponse(PostPaymentRequest postPaymentRequest, PaymentStatus status) {
    return PostPaymentResponse.builder()
        .id(UUID.randomUUID())
        .cardNumberLastFour(getLastFourDigits(postPaymentRequest.getCardNumber()))
        .status(status)
        .currency(postPaymentRequest.getCurrency())
        .amount(postPaymentRequest.getAmount())
        .expiryYear(postPaymentRequest.getExpiryYear())
        .expiryMonth(postPaymentRequest.getExpiryMonth())
        .build();
  }

  private int getLastFourDigits(String cardNumber) {
    return Integer.parseInt(cardNumber.substring(cardNumber.length()-4));
  }

}
