package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.*;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final BankSimulatorClient bankSimulatorClient;

  private final PaymentsRepository paymentsRepository;

  public PaymentGatewayService(BankSimulatorClient bankSimulatorClient,
      PaymentsRepository paymentsRepository) {

    this.bankSimulatorClient = bankSimulatorClient;
    this.paymentsRepository = paymentsRepository;
  }

  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return PaymentResponse.createFromProcessedResponse(
        paymentsRepository.get(id).orElseThrow(() ->
            new PaymentNotFoundException(
                "Invalid ID - Could not find payment associated with provided id"
            )));
  }

  public PaymentResponse processPayment(ProcessPaymentRequest paymentRequest) {

    LOG.debug("Start payment processing");
    AuthorisePaymentRequest authorisePaymentRequest = buildAuthorisePaymentRequest(paymentRequest);

    AuthorisePaymentResponse authorisePaymentResponse = bankSimulatorClient
        .authorisePayment(authorisePaymentRequest);

    ProcessedPaymentResponse processedPaymentResponse = buildProcessedPaymentResponse(
        paymentRequest,
        authorisePaymentResponse);

    paymentsRepository.add(processedPaymentResponse);

    return PaymentResponse.createFromProcessedResponse(processedPaymentResponse);
  }

  /**
   * Builder functions
   **/
  private static ProcessedPaymentResponse buildProcessedPaymentResponse(
      ProcessPaymentRequest paymentRequest,
      AuthorisePaymentResponse authorisePaymentResponse) {

    return ProcessedPaymentResponse.builder()
        .id(UUID.randomUUID())
        .status(authorisePaymentResponse.isAuthorized() ? PaymentStatus.AUTHORIZED
            : PaymentStatus.DECLINED)
        .authorisationCode(authorisePaymentResponse.getAuthorizationCode())
        .cardNumberLastFour(paymentRequest.getMaskedCardNumber())
        .expiryMonth(paymentRequest.getExpiryMonth())
        .expiryYear(paymentRequest.getExpiryYear())
        .currency(paymentRequest.getCurrency())
        .amount(paymentRequest.getAmount())
        .build();
  }

  private static AuthorisePaymentRequest buildAuthorisePaymentRequest(
      ProcessPaymentRequest paymentRequest) {

    return AuthorisePaymentRequest.builder()
        .cardNumber(paymentRequest.getCardNumber())
        .expiryDate(String.format("%02d/%d", paymentRequest.getExpiryMonth(),
            paymentRequest.getExpiryYear()))
        .currency(paymentRequest.getCurrency())
        .amount(paymentRequest.getAmount())
        .cvv(paymentRequest.getCvv())
        .build();
  }
}
