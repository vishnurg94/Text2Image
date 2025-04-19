package com.example.text2image.api;

public class ImageRequest {
    private String prompt;

    public ImageRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
} 