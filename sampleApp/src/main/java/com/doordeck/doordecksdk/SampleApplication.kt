package com.doordeck.doordecksdk

import android.app.Application
import android.content.Context
import com.doordeck.sdk.common.manager.Doordeck
import java.util.*
import kotlin.concurrent.schedule

class SampleApplication: Application() {


    init {
        instance = this
    }

    companion object {
        private var instance: SampleApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // initialize for any
        

    }
}