package com.checkout.payment.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentProcessingException extends RuntimeException{
  HttpStatus httpStatus;
  public PaymentProcessingException(String message) {
    super(message);
  }

  public PaymentProcessingException(String message, HttpStatus httpStatus) {
    super(message);
  }
}
