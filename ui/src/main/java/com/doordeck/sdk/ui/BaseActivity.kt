package com.doordeck.sdk.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doordeck.sdk.R
import com.doordeck.sdk.common.manager.DoordeckSDK
import io.reactivex.disposables.CompositeDisposable

internal open class BaseActivity : AppCompatActivity() {

    // to avoid memory leak
    protected var disposables = CompositeDisposable()

    // setup the theme in the activities that extends this one
    private fun getCurrentTheme(): Int {
        val lightTheme = DoordeckSDK.lightTheme
        return if (lightTheme) {
            R.style.lightTheme
        } else {
            R.style.darkTheme
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getCurrentTheme())
        super.onCreate(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        // free the memory
        disposables.dispose()
    }
}
