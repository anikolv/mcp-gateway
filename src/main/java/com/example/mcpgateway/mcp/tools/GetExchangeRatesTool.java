package com.example.mcpgateway.mcp.tools;

import com.example.mcpgateway.dto.ExchangeRate;
import com.example.mcpgateway.mcp.MCPTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetExchangeRatesTool implements MCPTool {

    private static final Logger log = LoggerFactory.getLogger(GetExchangeRatesTool.class);
    
    private final RestClient paymentGatewayRestClient;
    private final ObjectMapper objectMapper;

    public GetExchangeRatesTool(@Qualifier("paymentGatewayRestClient") RestClient paymentGatewayRestClient) {
        this.paymentGatewayRestClient = paymentGatewayRestClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "get_exchange_rates";
    }

    @Override
    public String getDescription() {
        return "Get currency exchange rates from the payment gateway. Can query specific currency pairs (e.g., USD to EUR) or get all available rates. Returns exchange rate and supported payment methods.";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "fromCurrency", Map.of(
                    "type", "string",
                    "description", "Source currency code (e.g., USD, EUR, GBP). Optional - if not provided, returns all rates."
                ),
                "toCurrency", Map.of(
                    "type", "string",
                    "description", "Target currency code (e.g., EUR, USD, CAD). Optional - if not provided, returns all rates for fromCurrency."
                )
            )
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        try {
            String fromCurrency = arguments.get("fromCurrency") != null 
                ? ((String) arguments.get("fromCurrency")).toUpperCase() 
                : null;
            String toCurrency = arguments.get("toCurrency") != null 
                ? ((String) arguments.get("toCurrency")).toUpperCase() 
                : null;
            
            log.info("Fetching exchange rates: from={}, to={}", fromCurrency, toCurrency);
            
            // Call payment gateway service with pagination
            String endpoint = "/paymentgw/config/exchange-rates?page=0&size=100";
            
            try {
                String responseBody = paymentGatewayRestClient.get()
                        .uri(endpoint)
                        .retrieve()
                        .body(String.class);
                
                if (responseBody != null) {
                    // Parse paginated response
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paginatedResponse = (Map<String, Object>) objectMapper.readValue(
                        responseBody, 
                        Map.class
                    );
                    
                    // Extract content array
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) paginatedResponse.get("content");
                    
                    // Convert to ExchangeRate objects
                    List<ExchangeRate> allRates = new ArrayList<>();
                    for (Map<String, Object> item : contentList) {
                        String json = objectMapper.writeValueAsString(item);
                        ExchangeRate rate = objectMapper.readValue(json, ExchangeRate.class);
                        allRates.add(rate);
                    }
                    
                    // Filter based on parameters
                    List<ExchangeRate> filteredRates = allRates;
                    
                    if (fromCurrency != null) {
                        final String fromCurr = fromCurrency;
                        filteredRates = filteredRates.stream()
                            .filter(rate -> rate.getFromCurrencyAlphabeticCode().equalsIgnoreCase(fromCurr))
                            .collect(Collectors.toList());
                    }
                    
                    if (toCurrency != null) {
                        final String toCurr = toCurrency;
                        filteredRates = filteredRates.stream()
                            .filter(rate -> rate.getToCurrencyAlphabeticCode().equalsIgnoreCase(toCurr))
                            .collect(Collectors.toList());
                    }
                    
                    return formatExchangeRates(filteredRates, fromCurrency, toCurrency);
                }
                
            } catch (Exception e) {
                log.error("Failed to fetch exchange rates from payment gateway", e);
                return String.format("Error: Unable to fetch exchange rates from payment gateway. Service may be unavailable. Details: %s", 
                    e.getMessage());
            }
            
            return "No exchange rate data available";
            
        } catch (Exception e) {
            log.error("Error executing get_exchange_rates tool", e);
            return "Error: " + e.getMessage();
        }
    }

    private String formatExchangeRates(List<ExchangeRate> rates, String fromCurrency, String toCurrency) {
        if (rates.isEmpty()) {
            if (fromCurrency != null && toCurrency != null) {
                return String.format("No exchange rate found for %s to %s currency pair.", 
                    fromCurrency, toCurrency);
            } else if (fromCurrency != null) {
                return String.format("No exchange rates found from %s currency.", fromCurrency);
            } else if (toCurrency != null) {
                return String.format("No exchange rates found to %s currency.", toCurrency);
            }
            return "No exchange rates configured in the system.";
        }
        
        StringBuilder result = new StringBuilder();
        
        if (fromCurrency != null && toCurrency != null && rates.size() == 1) {
            // Single specific rate query
            ExchangeRate rate = rates.get(0);
            result.append(String.format("✓ Exchange rate configured for %s → %s\n\n", 
                fromCurrency, toCurrency));
            result.append(String.format("Rate: 1 %s = %.6f %s\n", 
                rate.getFromCurrencyAlphabeticCode(), 
                rate.getExchangeRate(),
                rate.getToCurrencyAlphabeticCode()));
            result.append(String.format("Payment Methods: %d available\n", 
                rate.getPaymentMethods().size()));
            result.append("Methods: ").append(
                rate.getPaymentMethods().stream()
                    .limit(5)
                    .collect(Collectors.joining(", ")));
            if (rate.getPaymentMethods().size() > 5) {
                result.append(String.format(" ... and %d more", rate.getPaymentMethods().size() - 5));
            }
        } else {
            // Multiple rates
            result.append(String.format("Found %d exchange rate(s):\n\n", rates.size()));
            
            for (ExchangeRate rate : rates) {
                result.append(String.format("• %s → %s: %.6f (%.0f payment methods)\n",
                    rate.getFromCurrencyAlphabeticCode(),
                    rate.getToCurrencyAlphabeticCode(),
                    rate.getExchangeRate(),
                    (double) rate.getPaymentMethods().size()));
            }
        }
        
        return result.toString();
    }
}

