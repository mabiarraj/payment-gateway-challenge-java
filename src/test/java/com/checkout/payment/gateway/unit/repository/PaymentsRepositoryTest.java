package com.checkout.payment.gateway.unit.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.fixtures.Fixtures;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class PaymentsRepositoryTest {
  private final PaymentsRepository paymentsRepository = new PaymentsRepository();

  @Test
  void add_ShouldFindPayment_WhenPresent() {
    // given
    val postPaymentResponse = Fixtures.aPostPaymentResponseAuthorized();

    // when
    paymentsRepository.add(postPaymentResponse);

    // then
    val retrievedPaymentResponse = paymentsRepository.get(postPaymentResponse.getId());
    assertTrue(retrievedPaymentResponse.isPresent());
    assertEquals(postPaymentResponse, retrievedPaymentResponse.get());
  }

  @Test
  void add_ShouldReturnEmpty_WhenNotPresent() {
    // when
    val retrievedPaymentResponse = paymentsRepository.get(UUID.randomUUID());

    // then
    assertTrue(retrievedPaymentResponse.isEmpty());
  }

}