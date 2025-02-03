package com.checkout.payment.gateway.unit.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.PaymentProcessingException;
import com.checkout.payment.gateway.fixtures.Fixtures;
import com.checkout.payment.gateway.infrastructure.ImposterPaymentProcessor;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ImposterPaymentProcessorTest {

  private final String paymentUrl = "localhost:8080/payment";

  @Mock
  private RestTemplate restTemplate;

  private ImposterPaymentProcessor imposterPaymentProcessor;

  private BankProcessorRequest bankProcessorRequest;
  private BankProcessorResponse bankProcessorResponse;

  @BeforeEach
  void setUp() {
    bankProcessorRequest = Fixtures.aBankProcessorRequest();
    bankProcessorResponse = Fixtures.aBankProcessorResponseAuthorized();
    imposterPaymentProcessor = new ImposterPaymentProcessor(restTemplate, paymentUrl);
  }

  @Test
  void processPayment_ShouldReturnResponse_WhenRequestIsSuccessful() {
    // given
    lenient().when(restTemplate.postForEntity(eq(paymentUrl), any(), eq(BankProcessorResponse.class)))
        .thenReturn(new ResponseEntity<>(bankProcessorResponse, HttpStatus.OK));

    // when
    val result = imposterPaymentProcessor.processPayment(bankProcessorRequest);

    // then
    assertNotNull(result);
    assertTrue(result.getAuthorized());
    verify(restTemplate, times(1)).postForEntity(anyString(), any(BankProcessorRequest.class), eq(BankProcessorResponse.class));
  }

  @Test
  void testProcessPayment_ShouldThrowException_WhenRequestFails() {
    // given
    when(restTemplate.postForEntity(eq(paymentUrl), any(), eq(BankProcessorResponse.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

    // when
    Exception exception = assertThrows(PaymentProcessingException.class, () ->
        imposterPaymentProcessor.processPayment(bankProcessorRequest)
    );

    // then
    assertEquals("Payment processing failed with status: 500", exception.getMessage());
    verify(restTemplate, times(1)).postForEntity(anyString(), any(BankProcessorRequest.class), eq(BankProcessorResponse.class));
  }

  @Test
  void testProcessPayment_ShouldThrowException_WhenResponseIsNull() {
    // given
    when(restTemplate.postForEntity(anyString(), any(BankProcessorRequest.class), eq(BankProcessorResponse.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    // when
    Exception exception = assertThrows(RuntimeException.class, () ->
        imposterPaymentProcessor.processPayment(bankProcessorRequest)
    );

    // then
    assertEquals("Payment processing failed with status: 200", exception.getMessage());
    verify(restTemplate, times(1)).postForEntity(anyString(), any(BankProcessorRequest.class), eq(BankProcessorResponse.class));
  }
}
