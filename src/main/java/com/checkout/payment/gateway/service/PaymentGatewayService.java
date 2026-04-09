package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
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

  public PaymentGatewayService(BankSimulatorClient bankSimulatorClient, PaymentsRepository paymentsRepository) {
    this.bankSimulatorClient = bankSimulatorClient;
    this.paymentsRepository = paymentsRepository;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(ProcessPaymentRequest paymentRequest) {

    LOG.info("Start process payment");
    AuthorisePaymentRequest authorisePaymentRequest = AuthorisePaymentRequest.builder()
            .cardNumber(paymentRequest.getCardNumber())
            .expiryDate(String.format("%02d/%d", paymentRequest.getExpiryMonth(), paymentRequest.getExpiryYear()))
            .currency(paymentRequest.getCurrency())
            .amount(paymentRequest.getAmount())
            .cvv(paymentRequest.getCvv())
            .build();

    AuthorisePaymentResponse authorisePaymentResponse = bankSimulatorClient.authorisePayment(authorisePaymentRequest);

    String maskedCardNumber = maskCardNumber(paymentRequest.getCardNumber());

    PostPaymentResponse postPaymentResponse = PostPaymentResponse.builder()
            .id(UUID.randomUUID())
            .status(authorisePaymentResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED)
            .cardNumberLastFour(maskedCardNumber)
            .expiryMonth(paymentRequest.getExpiryMonth())
            .expiryYear(paymentRequest.getExpiryYear())
            .currency(paymentRequest.getCurrency())
            .amount(paymentRequest.getAmount())
            .build();

    paymentsRepository.add(postPaymentResponse);

    return postPaymentResponse;
  }

  private String maskCardNumber(String cardNumber) {
      int cardNumberLength = cardNumber.length();

      int lengthOfMask = cardNumberLength - 4;
      String mask = "*".repeat(lengthOfMask);
      String lastFourCardNumbers = cardNumber.substring(lengthOfMask);

      return mask + lastFourCardNumbers;
  }
}
