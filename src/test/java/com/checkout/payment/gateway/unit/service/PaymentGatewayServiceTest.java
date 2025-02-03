package com.checkout.payment.gateway.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.fixtures.Fixtures;
import com.checkout.payment.gateway.infrastructure.ImposterPaymentProcessor;
import com.checkout.payment.gateway.model.BankProcessorRequest;
import com.checkout.payment.gateway.model.BankProcessorResponse;
import com.checkout.payment.gateway.model.PaymentResponseTransformer;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {
  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private ImposterPaymentProcessor imposterPaymentProcessor;

  @Mock
  private PaymentResponseTransformer paymentResponseTransformer;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private UUID paymentId;
  private PostPaymentRequest paymentRequest;
  private PostPaymentResponse paymentResponse;
  private BankProcessorResponse bankProcessorResponse;

  @BeforeEach
  void setUp() {
    // given
    paymentId = UUID.randomUUID();
    paymentRequest = Fixtures.aPostPaymentRequest();
    paymentResponse = Fixtures.aPostPaymentResponseAuthorized();
    bankProcessorResponse = Fixtures.aBankProcessorResponseAuthorized();
  }

  @Test
  void getPaymentById_ShouldReturnPaymentResponse_WhenPaymentExists() {
    //given
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(paymentResponse));

    // when
    PostPaymentResponse result = paymentGatewayService.getPaymentById(paymentId);

    // then
    assertNotNull(result);
    assertEquals(paymentResponse, result);
    verify(paymentsRepository, times(1)).get(paymentId);
  }

  @Test
  void getPaymentById_ShouldThrowException_WhenPaymentNotFound() {
    // given
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.empty());

    // when
    assertThrows(EventProcessingException.class, () -> paymentGatewayService.getPaymentById(paymentId));

    // then
    verify(paymentsRepository, times(1)).get(paymentId);
  }

  @Test
  void testProcessPayment_ShouldProcessAndStorePayment_WhenAuthorized() {
    // given
    when(imposterPaymentProcessor.processPayment(any(BankProcessorRequest.class))).thenReturn(bankProcessorResponse);
    when(paymentResponseTransformer.requestToResponse(any(PostPaymentRequest.class), eq(PaymentStatus.AUTHORIZED)))
        .thenReturn(paymentResponse);

    // when
    PostPaymentResponse result = paymentGatewayService.processPayment(paymentRequest);

    // then
    assertNotNull(result);
    assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
    verify(imposterPaymentProcessor, times(1)).processPayment(any(BankProcessorRequest.class));
    verify(paymentResponseTransformer, times(1)).requestToResponse(any(PostPaymentRequest.class), eq(PaymentStatus.AUTHORIZED));
    verify(paymentsRepository, times(1)).add(result);
  }

  @Test
  void testProcessPayment_ShouldProcessAndStorePayment_WhenDeclined() {
    // Given
    bankProcessorResponse = Fixtures.aBankProcessorResponseUnauthorized();
    when(imposterPaymentProcessor.processPayment(any(BankProcessorRequest.class))).thenReturn(bankProcessorResponse);
    PostPaymentResponse declinedResponse = Fixtures.aPostPaymentResponseDeclined();
    when(paymentResponseTransformer.requestToResponse(any(PostPaymentRequest.class), eq(PaymentStatus.DECLINED)))
        .thenReturn(declinedResponse);

    // When
    PostPaymentResponse result = paymentGatewayService.processPayment(paymentRequest);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.DECLINED, result.getStatus());
    verify(imposterPaymentProcessor, times(1)).processPayment(any(BankProcessorRequest.class));
    verify(paymentResponseTransformer, times(1)).requestToResponse(any(PostPaymentRequest.class), eq(PaymentStatus.DECLINED));
    verify(paymentsRepository, times(1)).add(result);
  }

  @Test
  void testProcessPayment_ShouldThrowException_WhenExpiryDateIsInvalid() {
    // Given
    paymentRequest.setExpiryYear(1999);

    // When
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
      paymentGatewayService.processPayment(paymentRequest)
    );

    // Then
    assertEquals("Card expiration date must be in the future", exception.getMessage());
  }

  @Test
  void testProcessPayment_ShouldThrowException_WhenCurrencyIsInvalid() {
    // Given
    paymentRequest.setCurrency("BRL");

    // When
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
        paymentGatewayService.processPayment(paymentRequest)
    );

    // Then
    assertEquals("Currency is invalid", exception.getMessage());
  }

}
