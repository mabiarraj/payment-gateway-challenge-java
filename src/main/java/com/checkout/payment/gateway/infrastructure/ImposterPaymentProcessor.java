package com.checkout.payment.gateway.infrastructure;

import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.PaymentProcessingException;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ImposterPaymentProcessor implements PaymentProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final RestTemplate restTemplate;
  private final String paymentUrl;

  public ImposterPaymentProcessor(RestTemplate restTemplate, @Value("${payment.gateway.processor.url}") String paymentUrl) {
    this.restTemplate = restTemplate;
    this.paymentUrl = paymentUrl;
  }

  @Override
  public BankProcessorResponse processPayment(BankProcessorRequest bankProcessorRequest) {
    try {
      ResponseEntity<BankProcessorResponse> response = restTemplate.postForEntity(paymentUrl,
          bankProcessorRequest, BankProcessorResponse.class);
      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new PaymentProcessingException(
            String.format("Payment processing failed with status: %d", response.getStatusCode().value()),
            (HttpStatus) response.getStatusCode()
        );
      }
      return response.getBody();
    } catch (RestClientException restClientException) {
      LOG.error("Server error", restClientException);
      throw new PaymentProcessingException("Error when processing payment", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
