package com.example.mcpgateway.dto;

import jakarta.validation.constraints.NotBlank;

public class AskAiRequest {

    @NotBlank
    private String question;

    public AskAiRequest() {}

    public AskAiRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
