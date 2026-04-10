package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.PaymentAuthorisationException;
import com.checkout.payment.gateway.model.AuthorisePaymentRequest;
import com.checkout.payment.gateway.model.AuthorisePaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class BankSimulatorClient {
    private final RestClient restClient;

    public BankSimulatorClient(
        RestClient.Builder restClientBuilder,
        @Value("${bank-simulator.base-url}")String baseUrl)
    {
      HttpClient httpClient = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .connectTimeout(Duration.ofSeconds(5))
          .build();

      JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
      factory.setReadTimeout(Duration.ofSeconds(10));

      this.restClient = restClientBuilder
          .baseUrl(baseUrl)
          .requestFactory(factory)
          .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .build();
    }


  public AuthorisePaymentResponse authorisePayment(AuthorisePaymentRequest paymentRequest) {

        return restClient.post()
                .uri("/payments")
                .body(paymentRequest)
                .retrieve()
                .onStatus(status -> status.value() == 503, ((request, response) -> {
                        throw new PaymentAuthorisationException("Service Unavailable - Unable to authorise payment");
                    })
                )
                .body(AuthorisePaymentResponse.class);
    }
}
