package com.example.dessertrip.ui.collect.core.network;

public class UploadException extends Exception {
    public final ApiError error;

    public UploadException(ApiError error) {
        super(error.userMessage);
        this.error = error;
    }
}
