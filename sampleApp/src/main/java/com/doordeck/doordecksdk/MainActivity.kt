package com.doordeck.doordecksdk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.doordeck.doordecksdk.databinding.ActivityMainBinding
import com.doordeck.sdk.common.events.EventCallback
import com.doordeck.sdk.common.events.IEventCallback
import com.doordeck.sdk.common.events.UnlockCallback
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.manager.ScanType
import com.doordeck.sdk.common.models.DefaultDeviceWithUuid
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var disposables = CompositeDisposable()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Doordeck.initialize(applicationContext, getString(R.string.doordeck_api_key), true)

        binding.nfc.setOnClickListener { Doordeck.showUnlock(this) }
        binding.qrcode.setOnClickListener { unlockWithQRCode() }
        binding.uuid.setOnClickListener { showPopupUnlockWithUUID() }


        // the listeners are optional, only if you wish to listen to those events
        listenForEventsRx()
        listenForAllEventsCallback()
        listenForFewEventsCallback()

    }

    private fun unlockWithQRCode() {
        // the ScanType is optional, by default it's set to NFC
        // the callback is optional too
        Doordeck.showUnlock(this, ScanType.QR, defaultCallback)
    }

    private fun showPopupUnlockWithUUID() {
        binding.uuidText.error = null
        try {
            val uuid = binding.uuidText.text.toString()
            unlockWithUUID(uuid)
        } catch (illegalStateException: IllegalStateException) {
            binding.uuidText.error = illegalStateException.message
        }
    }

    private fun unlockWithUUID(uuid: String) {
        Doordeck.unlock(
            ctx = this,
            uuid = uuid,
            callback = defaultCallback,
        )
    }

    // subscribe to Doordeck.eventsObservable() that emits the events sent by the SDK
    private fun listenForEventsRx() {
        disposables.add(
            Doordeck.eventsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    Log.d("MainActivity", "event received : $event")
                }, Throwable::printStackTrace)
        )
    }


    // listener for all the events, optional
    private fun listenForAllEventsCallback() {
        Doordeck.withEventsCallback(object : IEventCallback {

            override fun noInternet() {
            }

            override fun networkError() {
            }

            override fun verificationCodeSent() {}

            override fun verificationCodeFailedSending() {
            }

            override fun codeVerificationSuccess() {
            }

            override fun codeVerificationFailed() {
            }

            override fun sdkError() {
            }

            override fun unlockSuccess() {
            }

            override fun unlockFailed() {
            }

            override fun resolveTileFailed() {
            }

            override fun resolveTileSuccess() {
            }

            override fun unlockedInvalidTileID() {
            }
        })
    }


    // listener for few the events, optional
    // you need to override the method you wish to get the event from
    private fun listenForFewEventsCallback() {
        Doordeck.withEventsCallback(object : EventCallback() {

            override fun networkError() {
            }

            override fun sdkError() {
            }

            override fun unlockSuccess() {
            }

            override fun unlockFailed() {
            }
        })
    }

    public override fun onDestroy() {
        super.onDestroy()
        // free the memory
        disposables.dispose()
    }

    private val defaultCallback: UnlockCallback = object : UnlockCallback {
        override fun invalidAuthToken() {
            Toast.makeText(applicationContext, "Invalid auth token", LENGTH_SHORT).show()
        }

        override fun notAuthenticated() {
            Toast.makeText(applicationContext, "Not authenticated", LENGTH_SHORT).show()
        }

        override fun unlockSuccess() {
            Toast.makeText(applicationContext, "Unlock Success", LENGTH_SHORT).show()
        }

        override fun unlockFailed() {
            Toast.makeText(applicationContext, "Unlock Failed", LENGTH_SHORT).show()
        }

        override fun verificationNeeded() {
            Doordeck.showVerificationScreen(this@MainActivity)
            Toast.makeText(applicationContext, "2fa needed", LENGTH_SHORT).show()
        }
    }

}
