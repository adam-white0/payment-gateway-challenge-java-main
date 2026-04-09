package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.PaymentAuthorisationException;
import com.checkout.payment.gateway.model.AuthorisePaymentRequest;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankSimulatorClient {
    private final RestClient restClient;

    public AuthorisePaymentResponse authorisePayment(AuthorisePaymentRequest paymentRequest) {

        log.info(String.valueOf(paymentRequest));
        return restClient.post()
                .uri("/payments")
                .body(paymentRequest)
                .retrieve()
                .onStatus(status -> status.value() == 503, ((request, response) -> {
                        throw new PaymentAuthorisationException("No payment could be created as invalid information was supplied");
                    })
                )
                .body(AuthorisePaymentResponse.class);
    }
}
