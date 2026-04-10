package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.PaymentAuthorisationException;
import com.checkout.payment.gateway.model.AuthorisePaymentRequest;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;

@Component
public class BankSimulatorClient {
    private final RestClient restClient;

    public BankSimulatorClient(
        RestClient.Builder restClientBuilder,
        @Value("${bank-simulator.base-url}")String baseUrl)
    {
      HttpClient httpClient = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .build();

      this.restClient = restClientBuilder
          .baseUrl(baseUrl)
          .requestFactory(new JdkClientHttpRequestFactory(httpClient))
          .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .build();
    }


  public AuthorisePaymentResponse authorisePayment(AuthorisePaymentRequest paymentRequest) {

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
