package com.checkout.payment.gateway.infrastructure;

import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;

public interface PaymentProcessor {
  BankProcessorResponse processPayment(BankProcessorRequest bankProcessorRequest);
}
