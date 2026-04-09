package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @PostMapping("/process-payment")
  public ResponseEntity<PostPaymentResponse> processPayment(
          @RequestBody @Valid ProcessPaymentRequest processPaymentRequest
        ) {

    PostPaymentResponse paymentResponse = paymentGatewayService.processPayment(processPaymentRequest);
    return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
  }

  @GetMapping("/payment/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }
}
