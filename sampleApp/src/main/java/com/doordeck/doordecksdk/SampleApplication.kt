package com.doordeck.doordecksdk

import android.app.Application
import android.content.Context
import com.doordeck.sdk.common.manager.Doordeck
import java.util.*
import kotlin.concurrent.schedule

class SampleApplication: Application() {


//    override fun onCreate() {
//        super.onCreate()
//        Doordeck.initialize(SampleApplication().applicationContext, false)
//
//    }

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

        // Use ApplicationContext.
        // example: SharedPreferences etc...
        val context: Context = SampleApplication.applicationContext()
        Timer("SettingUp", false).schedule(2000) {
            Doordeck.initialize(context, null, true)
        }

    }
}