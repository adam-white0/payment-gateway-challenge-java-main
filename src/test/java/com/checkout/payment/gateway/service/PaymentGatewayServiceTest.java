package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.model.ProcessedPaymentResponse;
import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    PaymentResponse expectedResponse = buildPaymentResponse(
        PaymentStatus.AUTHORIZED,
        "***********2345"
    );

    AuthorisePaymentResponse authPaymentResponse = new AuthorisePaymentResponse();
    authPaymentResponse.setAuthorized(true);
    authPaymentResponse.setAuthorizationCode("0bb07405-6d44-4b50-a14f-7ae0beff13ad");

    when(bankSimulatorClient.authorisePayment(any())).thenReturn(authPaymentResponse);

    PaymentResponse response = paymentGatewayService.processPayment(request);

    ArgumentCaptor<ProcessedPaymentResponse> captor = ArgumentCaptor
        .forClass(ProcessedPaymentResponse.class);
    verify(paymentsRepository).add(captor.capture());

    // Assertion on the object saved in repository
    ProcessedPaymentResponse savedResponse = captor.getValue();
    assertThat(savedResponse.getStatus()).isEqualTo(expectedResponse.getStatus());
    assertThat(savedResponse.getAuthorisationCode()).isEqualTo(
        authPaymentResponse.getAuthorizationCode());

    // Assertion on the returned object
    assertThat(response)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedResponse);
  }

  @Test
  void shouldReturnResponseWithDeclinedStatusWhenBankDeclinesPayment() {
    ProcessPaymentRequest request = buildProcessPaymentRequest("123456789012344");

    PaymentResponse expectedResponse = buildPaymentResponse(
        PaymentStatus.DECLINED,
        "***********2344"
    );

    AuthorisePaymentResponse authPaymentResponse = new AuthorisePaymentResponse();
    authPaymentResponse.setAuthorized(false);

    when(bankSimulatorClient.authorisePayment(any())).thenReturn(authPaymentResponse);

    PaymentResponse response = paymentGatewayService.processPayment(request);

    ArgumentCaptor<ProcessedPaymentResponse> captor = ArgumentCaptor
        .forClass(ProcessedPaymentResponse.class);
    verify(paymentsRepository).add(captor.capture());

    // Assertion on the object saved in repository
    ProcessedPaymentResponse savedResponse = captor.getValue();
    assertThat(savedResponse.getStatus()).isEqualTo(expectedResponse.getStatus());
    assertThat(savedResponse.getAuthorisationCode()).isNull();

    // Assertion on the returned object
    assertThat(response)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expectedResponse);
  }

  @Test
  void shouldReturnPaymentDetailsWhenPaymentExists() {
    UUID paramId = UUID.randomUUID();
    ProcessedPaymentResponse processedPaymentResponse = buildProcessedPaymentResponse(
        paramId
    );

    PaymentResponse expectedResponse = PaymentResponse
        .createFromProcessedResponse(processedPaymentResponse);

    when(paymentsRepository.get(paramId)).thenReturn(Optional.of(processedPaymentResponse));

    PaymentResponse result = paymentGatewayService.getPaymentById(paramId);

    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void shouldThrowExceptionWhenPaymentNotFound() {
    UUID paramId = UUID.randomUUID();

    assertThatThrownBy(() -> paymentGatewayService.getPaymentById(paramId))
        .isInstanceOf(PaymentNotFoundException.class)
        .hasMessage("Invalid ID - Could not find payment associated with provided id");
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

  private PaymentResponse buildPaymentResponse(
      PaymentStatus status,
      String cardNumberLastFour
  ) {
    return PaymentResponse.builder()
        .id(null)
        .status(status)
        .cardNumberLastFour(cardNumberLastFour)
        .expiryMonth(12)
        .expiryYear(2027)
        .currency("GBP")
        .amount(2000)
        .build();
  }

  private ProcessedPaymentResponse buildProcessedPaymentResponse(
      UUID id
  ) {
    return ProcessedPaymentResponse.builder()
        .id(id)
        .status(PaymentStatus.AUTHORIZED)
        .authorisationCode("0bb07405-6d44-4b50-a14f-7ae0beff13ad")
        .cardNumberLastFour("***********2345")
        .expiryMonth(12)
        .expiryYear(2027)
        .currency("GBP")
        .amount(2000)
        .build();
  }
}