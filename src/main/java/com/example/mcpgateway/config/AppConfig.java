package com.example.mcpgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${llm.base-url}")
    private String llmBaseUrl;

    @Value("${llm.api-key}")
    private String llmApiKey;

    @Value("${backend.services.payment-gateway.url}")
    private String paymentGatewayServiceUrl;

    @Bean
    public RestClient llmRestClient() {
        return RestClient.builder()
                .baseUrl(llmBaseUrl)
                .defaultHeader("Authorization", "Bearer " + llmApiKey)
                .defaultHeader("HTTP-Referer", "http://localhost:8082")
                .defaultHeader("X-Title", "MCP-Gateway")
                .build();
    }

    @Bean(name = "paymentGatewayRestClient")
    public RestClient paymentGatewayRestClient() {
        return RestClient.builder()
                .baseUrl(paymentGatewayServiceUrl)
                .build();
    }
}
