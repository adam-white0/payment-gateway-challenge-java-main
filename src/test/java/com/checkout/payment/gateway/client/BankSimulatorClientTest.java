package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.PaymentAuthorisationInvalidException;
import com.checkout.payment.gateway.exception.PaymentAuthorisationUnavailableException;
import com.checkout.payment.gateway.model.AuthorisePaymentRequest;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;


import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RestClientTest(BankSimulatorClient.class)
@EnableWireMock(@ConfigureWireMock(
    name = "bank-simulator",
    baseUrlProperties = "bank-simulator.base-url"
))
class BankSimulatorClientTest {

  @Autowired
  private BankSimulatorClient bankSimulatorClient;

  @InjectWireMock("bank-simulator")
  private WireMockServer wireMock;

  @Test
  void shouldReturnAuthorisedResponseOnSuccessWhenCardNumberIsOdd() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(okJson("""
                    {
                      "authorized": true,
                      "authorization_code": "0bb07405-6d44-4b50-a14f-7ae0beff13ad"
                    }
                """)));

    AuthorisePaymentResponse result = bankSimulatorClient.authorisePayment(
        buildAuthorisePaymentRequest("123456789012345"));

    assertThat(result.isAuthorized()).isTrue();
  }

  @Test
  void shouldReturnUnauthorisedResponseOnSuccessWhenCardNumberIsEven() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(okJson("""
                    {"authorized": false}
                """)));

    AuthorisePaymentResponse result = bankSimulatorClient.authorisePayment(
        buildAuthorisePaymentRequest("123456789012344"));

    assertThat(result.isAuthorized()).isFalse();
  }

  @Test
  void shouldThrowPaymentAuthorisationUnavailableExceptionOn503WhenCardNumberEndsInZero() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(serviceUnavailable()));

    assertThatThrownBy(() -> bankSimulatorClient.authorisePayment(
        buildAuthorisePaymentRequest("123456789012340")))
        .isInstanceOf(PaymentAuthorisationUnavailableException.class)
        .hasMessage("Service Unavailable - Unable to authorise payment");
  }

  @Test
  void shouldThrowPaymentAuthorisationInvalidExceptionOn400WhenCardDetailsMissing() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(badRequest()));

    assertThatThrownBy(() -> bankSimulatorClient.authorisePayment(
        buildAuthorisePaymentRequest(null)))
        .isInstanceOf(PaymentAuthorisationInvalidException.class)
        .hasMessage("Request rejected - missing card details");
  }

  private AuthorisePaymentRequest buildAuthorisePaymentRequest (String cardNumber) {

    return AuthorisePaymentRequest.builder()
        .cardNumber(cardNumber)
        .expiryDate("12/2027")
        .currency("GBP")
        .amount(2000)
        .cvv("123")
        .build();
  }
}
