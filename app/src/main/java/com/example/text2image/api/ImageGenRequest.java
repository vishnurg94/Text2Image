package com.example.text2image.api;

public class ImageGenRequest {
    private String signature;
    private String prompt;

    public ImageGenRequest(String signature, String prompt) {
        this.signature = signature;
        this.prompt = prompt;
    }

    public String getSignature() {
        return signature;
    }

    public String getPrompt() {
        return prompt;
    }
}
