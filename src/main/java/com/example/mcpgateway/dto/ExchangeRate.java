package com.example.mcpgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExchangeRate {
    
    @JsonProperty("fromCurrencyAlphabeticCode")
    private String fromCurrencyAlphabeticCode;
    
    @JsonProperty("fromCurrencyNumericCode")
    private String fromCurrencyNumericCode;
    
    @JsonProperty("toCurrencyAlphabeticCode")
    private String toCurrencyAlphabeticCode;
    
    @JsonProperty("toCurrencyNumericCode")
    private String toCurrencyNumericCode;
    
    @JsonProperty("exchangeRate")
    private Double exchangeRate;
    
    @JsonProperty("paymentMethods")
    private List<String> paymentMethods;

    public ExchangeRate() {}

    public String getFromCurrencyAlphabeticCode() {
        return fromCurrencyAlphabeticCode;
    }

    public void setFromCurrencyAlphabeticCode(String fromCurrencyAlphabeticCode) {
        this.fromCurrencyAlphabeticCode = fromCurrencyAlphabeticCode;
    }

    public String getFromCurrencyNumericCode() {
        return fromCurrencyNumericCode;
    }

    public void setFromCurrencyNumericCode(String fromCurrencyNumericCode) {
        this.fromCurrencyNumericCode = fromCurrencyNumericCode;
    }

    public String getToCurrencyAlphabeticCode() {
        return toCurrencyAlphabeticCode;
    }

    public void setToCurrencyAlphabeticCode(String toCurrencyAlphabeticCode) {
        this.toCurrencyAlphabeticCode = toCurrencyAlphabeticCode;
    }

    public String getToCurrencyNumericCode() {
        return toCurrencyNumericCode;
    }

    public void setToCurrencyNumericCode(String toCurrencyNumericCode) {
        this.toCurrencyNumericCode = toCurrencyNumericCode;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public List<String> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<String> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
}

