package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.infrastructure.ImposterPaymentProcessor;
import com.checkout.payment.gateway.infrastructure.PaymentProcessor;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import com.checkout.payment.gateway.model.PaymentResponseTransformer;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final PaymentProcessor paymentProcessor;
  private final PaymentResponseTransformer paymentResponseTransformer;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      PaymentProcessor paymentProcessor, PaymentResponseTransformer paymentResponseTransformer) {
    this.paymentsRepository = paymentsRepository;
    this.paymentProcessor = paymentProcessor;
    this.paymentResponseTransformer = paymentResponseTransformer;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    BankProcessorRequest bankProcessorRequest = createBankProcessorRequest(paymentRequest);
    BankProcessorResponse processorResponse = paymentProcessor.processPayment(bankProcessorRequest);

    PaymentStatus paymentStatus =
        processorResponse.getAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    PostPaymentResponse paymentResponse = paymentResponseTransformer.requestToResponse(
        paymentRequest, paymentStatus);

    paymentsRepository.add(paymentResponse);
    return paymentResponse;
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
