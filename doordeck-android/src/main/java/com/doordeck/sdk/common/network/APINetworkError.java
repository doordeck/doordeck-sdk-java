package com.doordeck.sdk.common.network;

public class APINetworkError {

    public String errorMessage;
    public int errorStatus;

    public APINetworkError(String errorMessage, int errorStatus) {
        this.errorMessage = errorMessage;
        this.errorStatus = errorStatus;
    }
}
