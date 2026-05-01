package com.tutict.eip.harness.domain;

import jakarta.validation.constraints.NotBlank;

public class GenerateRequest {

    @NotBlank(message = "prompt must not be blank")
    private String prompt;

    public GenerateRequest() {
    }

    public GenerateRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
