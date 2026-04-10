package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private BankSimulatorClient bankSimulatorClient;

  @Mock
  private PaymentsRepository paymentsRepository;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  @Test
  void shouldReturnResponseWithAuthorisedStatusWhenBankApprovesPayment() {
    ProcessPaymentRequest request = buildProcessPaymentRequest("123456789012345");

    PostPaymentResponse expectedResponse = buildPostPaymentResponse(
        null,
        PaymentStatus.AUTHORIZED,
        "***********2345"
    );

    AuthorisePaymentResponse authPaymentResponse = new AuthorisePaymentResponse();
    authPaymentResponse.setAuthorized(true);

    when(bankSimulatorClient.authorisePayment(any())).thenReturn(authPaymentResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(request);

    assertThat(response)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedResponse);
    verify(paymentsRepository).add(response);
  }

  @Test
  void shouldReturnResponseWithDeclinedStatusWhenBankDeclinesPayment() {
    ProcessPaymentRequest request = buildProcessPaymentRequest("123456789012344");

    PostPaymentResponse expectedResponse = buildPostPaymentResponse(
        null,
        PaymentStatus.DECLINED,
        "***********2344"
    );

    AuthorisePaymentResponse authPaymentResponse = new AuthorisePaymentResponse();
    authPaymentResponse.setAuthorized(false);

    when(bankSimulatorClient.authorisePayment(any())).thenReturn(authPaymentResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(request);

    assertThat(response)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedResponse);
    verify(paymentsRepository).add(response);

  }

  @Test
  void shouldReturnPaymentDetailsWhenPaymentExists() {
    UUID paramId = UUID.randomUUID();
    PostPaymentResponse expectedResponse = buildPostPaymentResponse(
        paramId,
        PaymentStatus.AUTHORIZED,
        "***********2345"
    );

    when(paymentsRepository.get(paramId)).thenReturn(Optional.of(expectedResponse));

    PostPaymentResponse result = paymentGatewayService.getPaymentById(paramId);

    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void shouldThrowExceptionWhenPaymentNotFound() {
    UUID paramId = UUID.randomUUID();

    assertThatThrownBy(() -> paymentGatewayService.getPaymentById(paramId))
        .isInstanceOf(EventProcessingException.class)
        .hasMessage("Invalid ID");
  }

  private ProcessPaymentRequest buildProcessPaymentRequest(
      String cardNumber
  ) {
    return ProcessPaymentRequest.builder()
        .cardNumber(cardNumber)
        .expiryMonth(12)
        .expiryYear(2027)
        .currency("GBP")
        .amount(2000)
        .cvv("123")
        .build();
  }

  private PostPaymentResponse buildPostPaymentResponse(
      UUID id,
      PaymentStatus status,
      String cardNumberLastFour
  ) {
    return PostPaymentResponse.builder()
        .id(id)
        .status(status)
        .cardNumberLastFour(cardNumberLastFour)
        .expiryMonth(12)
        .expiryYear(2027)
        .currency("GBP")
        .amount(2000)
        .build();
  }
}