package com.reliaquest.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${employee.service.baseUrl}")
    private String employeeServiceBaseUrl;

    @Bean
    public WebClient employeeWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(employeeServiceBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

    }
}
