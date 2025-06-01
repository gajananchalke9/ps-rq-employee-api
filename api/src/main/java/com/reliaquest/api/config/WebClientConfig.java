package com.reliaquest.api.config;

import com.reliaquest.api.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);


    @Value("${employee.service.baseUrl}")
    private String employeeServiceBaseUrl;

    @Bean
    public WebClient employeeWebClient(WebClient.Builder webClientBuilder) {
        logger.info("Initializing employeeWebClient with base URL: {}", employeeServiceBaseUrl);
        return webClientBuilder
                .baseUrl(employeeServiceBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(rateLimitFilter())
                .build();

    }

    private ExchangeFilterFunction rateLimitFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                logger.warn("Received 429 Too Many Requests from external service at {}", employeeServiceBaseUrl);
                return clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("Rate limit exceeded")
                        .flatMap(bodyText -> {
                            logger.debug("Body of 429 response: {}", bodyText);
                            return Mono.error(
                                    new RateLimitExceededException(
                                            "External service rate limit (429) encountered: " + bodyText
                                    )
                            );
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}
