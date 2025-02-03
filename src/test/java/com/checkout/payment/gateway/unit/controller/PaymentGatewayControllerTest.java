package com.checkout.payment.gateway.unit.controller;

import com.checkout.payment.gateway.presentation.PaymentGatewayController;
import com.checkout.payment.gateway.fixtures.Fixtures;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentGatewayController.class)
class PaymentGatewayControllerTest {
  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @Test
  void getPostPaymentEventById_HappyPath() throws Exception {
    // given
    UUID paymentId = UUID.fromString("BBE8EB5F-3DCC-492E-978D-2B812AC34FEF");
    val paymentResponse = Fixtures.aPostPaymentResponseAuthorized();
    when(paymentGatewayService.getPaymentById(any())).thenReturn(Fixtures.aPostPaymentResponseAuthorized());

    // when
    mockMvc.perform(MockMvcRequestBuilders.get("/payment/{id}", paymentId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(paymentResponse.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentResponse.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.getAmount()));

    //then
    verify(paymentGatewayService, times(1)).getPaymentById(paymentId);
  }

  @Test
  void createPayment_HappyPath() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    PostPaymentResponse paymentResponse = Fixtures.aPostPaymentResponseAuthorized();

    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class))).thenReturn(paymentResponse);

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(paymentResponse.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentResponse.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.getAmount()));

    // then
    verify(paymentGatewayService, times(1)).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void createPayment_InvalidCardNumber() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    paymentRequest.setCardNumber("1");

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(".cardNumber").value("Card number must be 16 digits"));

    // then
    verify(paymentGatewayService, times(0)).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void createPayment_InvalidCvv() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    paymentRequest.setCvv("1");

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(".cvv").value("CVV must be 3 digits"));

    // then
    verify(paymentGatewayService, times(0)).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void createPayment_InvalidExpiryMonth() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    paymentRequest.setExpiryMonth(13);

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(".expiryMonth").value("Expiration month must be between 1 and 12"));

    // then
    verify(paymentGatewayService, times(0)).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void createPayment_InvalidExpiryYear() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    paymentRequest.setExpiryYear(24);

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(".expiryYear").value("Expiration year must be at least 2025"));

    // then
    verify(paymentGatewayService, times(0)).processPayment(any(PostPaymentRequest.class));
  }

  @Test
  void createPayment_InvalidAmount() throws Exception {
    // Given
    PostPaymentRequest paymentRequest = Fixtures.aPostPaymentRequest();
    paymentRequest.setAmount(BigDecimal.ZERO);

    // When
    mockMvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest))) // Serialize request object to JSON
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(".amount").value("Amount must be greater than 0"));

    // then
    verify(paymentGatewayService, times(0)).processPayment(any(PostPaymentRequest.class));
  }
}