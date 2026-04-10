package com.checkout.payment.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;

@Configuration
public class ApplicationConfiguration {

//  @Bean
//  public RestClient restClient(@Value("${bank-simulator.base-url}") String baseUrl) {
//    HttpClient httpClient = HttpClient.newBuilder()
//        .version(HttpClient.Version.HTTP_1_1)
//        .build();
//
//    return RestClient.builder()
//            .baseUrl(baseUrl)
//            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .build();
//  }
}
