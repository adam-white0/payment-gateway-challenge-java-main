package com.checkout.payment.gateway.controller;


import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(
    name = "bank-simulator",
    baseUrlProperties = "bank-simulator.base-url"
))
class PaymentGatewayControllerIT {

  @InjectWireMock("bank-simulator")
  private WireMockServer wireMock;

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  //Processing payments
  @Test
  void shouldReturn400_whenPaymentDetailsAreInvalid() throws Exception {
    String requestBody = """
            {
                "cardNumber": "1234567890123",
                "expiryMonth": 1,
                "expiryYear": 2020,
                "currency": "ABCD",
                "amount": null,
                "cvv": "12345"
            }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/process-payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.paymentStatus").value(
                PaymentStatus.REJECTED.getName()))
            .andExpect(jsonPath("$.errors.cardNumber").exists())
            .andExpect(jsonPath("$.errors.expiryYear").exists())
            .andExpect(jsonPath("$.errors.currency").exists())
            .andExpect(jsonPath("$.errors.amount").exists())
            .andExpect(jsonPath("$.errors.cvv").exists());
  }

  @Test
  void shouldReturn400_whenCardNumberEndsInZero() throws Exception {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(serviceUnavailable()));

    String requestBody = """
            {
                "cardNumber": "123456789012340",
                "expiryMonth": 12,
                "expiryYear": 2027,
                "currency": "GBP",
                "amount": 2000,
                "cvv": "123"
            }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/process-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.paymentStatus").value(PaymentStatus.REJECTED.getName()))
        .andExpect(jsonPath("$.errors.cardNumber").exists());
  }

  @Test
  void shouldReturn200WhenBankAuthorizesPayment() throws Exception {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(okJson("""
                    {
                      "authorized": true,
                      "authorization_code": "0bb07405-6d44-4b50-a14f-7ae0beff13ad"
                    }
                """)));

    ProcessPaymentRequest paymentRequest = ProcessPaymentRequest.builder()
        .cardNumber("123456789012345")
        .expiryMonth(12)
        .expiryYear(2027)
        .currency("GBP")
        .amount(2000)
        .cvv("123")
        .build();

    String jsonRequest = objectMapper.writeValueAsString(paymentRequest);

    mvc.perform(MockMvcRequestBuilders.post("/process-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(
            paymentRequest.getMaskedCardNumber())
        )
        .andExpect(jsonPath("$.expiryMonth").value(paymentRequest.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentRequest.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentRequest.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentRequest.getAmount()));
}

  // Retrieving payment details
  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = PostPaymentResponse.builder()
            .id(UUID.randomUUID())
            .amount(10)
            .currency("USD")
            .status(PaymentStatus.AUTHORIZED)
            .expiryMonth(12)
            .expiryYear(2024)
            .cardNumberLastFour("4321")
            .build();

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
}
