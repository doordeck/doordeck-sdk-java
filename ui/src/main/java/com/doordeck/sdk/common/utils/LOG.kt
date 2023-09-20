package com.doordeck.sdk.common.utils

import android.util.Log
import com.github.doordeck.ui.BuildConfig

// to display the messages only in debug mode
internal object LOG {
    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.e(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }
}
