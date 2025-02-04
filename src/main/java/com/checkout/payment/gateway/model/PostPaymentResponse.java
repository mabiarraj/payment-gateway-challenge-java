package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PostPaymentResponse {
  private UUID id;
  private PaymentStatus status;
  private int cardNumberLastFour;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private BigDecimal amount;

  public PostPaymentResponse(PostPaymentRequest postPaymentRequest, PaymentStatus status) {
    this.id = UUID.randomUUID();
    this.cardNumberLastFour = getLastFourDigits(postPaymentRequest.getCardNumber());
    this.status = status;
    this.currency = postPaymentRequest.getCurrency();
    this.amount = postPaymentRequest.getAmount();
    this.expiryYear = postPaymentRequest.getExpiryYear();
    this.expiryMonth = postPaymentRequest.getExpiryMonth();
  }

  private int getLastFourDigits(String cardNumber) {
    return Integer.parseInt(cardNumber.substring(cardNumber.length()-4));
  }

  @Override
  public String toString() {
    return "GetPaymentResponse{" +
        "id=" + id +
        ", status=" + status +
        ", cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
