package com.checkout.payment.gateway.integration.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.fixtures.Fixtures;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerIntTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        4321,
        12,
        2024,
        "USD",
        BigDecimal.valueOf(10.57)
    );

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void whenCreatingValidPaymentAuthorizedReturnDetails() throws Exception {
    PostPaymentRequest payment = Fixtures.aPostPaymentRequest();
    mvc.perform(
        MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payment))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumber().substring(payment.getCardNumber().length()-4)))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenCreatingPaymentUnauthorizedReturnDetails() throws Exception {
    PostPaymentRequest payment = new PostPaymentRequest();
    payment.setCardNumber("2222405343248112");
    payment.setCvv("456");
    payment.setAmount(BigDecimal.valueOf(60000));
    payment.setCurrency("USD");
    payment.setExpiryMonth(1);
    payment.setExpiryYear(2026);

    mvc.perform(
            MockMvcRequestBuilders.post("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumber().substring(payment.getCardNumber().length()-4)))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

}
