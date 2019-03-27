package com.doordeck.sdk.common.executor;


public interface MainThread {
    void post(final Runnable runnable);
}

