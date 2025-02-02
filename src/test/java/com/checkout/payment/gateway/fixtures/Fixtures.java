package com.checkout.payment.gateway.fixtures;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import lombok.val;
import java.util.UUID;

public class Fixtures {
  public static PostPaymentRequest aPostPaymentRequest() {
    val paymentRequest = new PostPaymentRequest();
    paymentRequest.setAmount(100);
    paymentRequest.setCurrency("USD");
    paymentRequest.setCvv(123);
    paymentRequest.setExpiryMonth(12);
    paymentRequest.setExpiryYear(2025);
    paymentRequest.setCardNumber("2222405343248877");
    return paymentRequest;
  }

  public static PostPaymentResponse aPostPaymentResponseAuthorized() {
    return PostPaymentResponse.builder()
        .id(UUID.randomUUID())
        .amount(100)
        .currency("USD")
        .status(PaymentStatus.AUTHORIZED)
        .expiryMonth(12)
        .expiryYear(2025)
        .build();
  }

  public static PostPaymentResponse aPostPaymentResponseDeclined() {
    return PostPaymentResponse.builder()
        .id(UUID.randomUUID())
        .amount(100)
        .currency("USD")
        .status(PaymentStatus.DECLINED)
        .expiryMonth(12)
        .expiryYear(2025)
        .build();
  }

  public static BankProcessorRequest aBankProcessorRequest() {
    return BankProcessorRequest.builder()
        .card_number( "1234567812345678")
        .cvv(123)
        .expiry_date("12/25")
        .amount(100)
        .currency("USD")
        .build();
  }

  public static BankProcessorResponse aBankProcessorResponseAuthorized() {
    return new BankProcessorResponse(true, "authorization-code");
  }

  public static BankProcessorResponse aBankProcessorResponseUnauthorized() {
    return new BankProcessorResponse(false, null);
  }
}
