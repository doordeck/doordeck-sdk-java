package com.doordeck.sdk.common.utils

import android.os.Handler
import android.os.Looper

fun onUiThread(post: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        post()
    }
}