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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayControllerTest {
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private PaymentGatewayService paymentGatewayService;

  @InjectMocks
  private PaymentGatewayController paymentGatewayController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(paymentGatewayController).build();
  }

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
}