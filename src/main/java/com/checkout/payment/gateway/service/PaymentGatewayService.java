package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.infrastructure.PaymentProcessor;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final PaymentProcessor paymentProcessor;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      PaymentProcessor paymentProcessor) {
    this.paymentsRepository = paymentsRepository;
    this.paymentProcessor = paymentProcessor;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    paymentRequest.validate();
    BankProcessorRequest bankProcessorRequest = createBankProcessorRequest(paymentRequest);
    BankProcessorResponse processorResponse = paymentProcessor.processPayment(bankProcessorRequest);
    PaymentStatus paymentStatus =
        processorResponse.getAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    PostPaymentResponse postPaymentResponse = new PostPaymentResponse(paymentRequest, paymentStatus);
    paymentsRepository.add(postPaymentResponse);
    return postPaymentResponse;
  }

  private static BankProcessorRequest createBankProcessorRequest(PostPaymentRequest paymentRequest) {
    return BankProcessorRequest.builder()
        .cvv(paymentRequest.getCvv())
        .card_number(paymentRequest.getCardNumber())
        .amount(paymentRequest.getAmount())
        .currency(paymentRequest.getCurrency())
        .expiry_date(paymentRequest.getExpiryDate())
        .build();
  }
}
