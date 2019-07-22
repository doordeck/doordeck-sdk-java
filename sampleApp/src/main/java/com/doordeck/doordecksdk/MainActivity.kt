package com.doordeck.doordecksdk

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.doordeck.sdk.common.events.EventCallback
import com.doordeck.sdk.common.events.IEventCallback
import com.doordeck.sdk.common.events.ShareCallback
import com.doordeck.sdk.common.events.UnlockCallback
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.manager.ScanType
import com.google.common.base.Optional
import com.doordeck.sdk.dto.operation.ImmutableAddUserOperation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.Instant
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*


class MainActivity : AppCompatActivity() {

    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Doordeck.initialize(applicationContext, getString(R.string.doordeck_api_key), true);

        nfc.setOnClickListener { Doordeck.showUnlock(this) }
        qrcode.setOnClickListener { unlockWithQRCode() }
        share.setOnClickListener { share() }

        // the listeners are optional, only if you wish to listen to those events
        listenForEventsRx()
        listenForAllEventsCallback()
        listenForFewEventsCallback()

    }

    private fun share () {

        val userId = UUID.fromString("2300db50-a29e-11e6-a799-e9e14b3b97b9");
        val pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq6pVdr+1wpuOSXkEuxerEN2jBnAu1rKTwhZSk+GEa5DiiQH7xZ6NDY7V7hzHdIJ26IsAV+WqjwxXd7L2Niso7tXQz73UXwLLky23uYuyOc7QXrHEIbHlgicNiGESuNwgHskxZG2elTtj6tbVChKX2OQSdD75vExlBCpWgIPuDvqXlItySapZDPNgf//eBqq8DUk87EzSe3Yo0Gy82fF5NF97f6v64a4OkOYNepB2wXFNZ2G3rzGiGCgRyZZWmA7dRvRijzPxzO4zRujScCAwdJ1RBPPv3fqK2KOWV24qfujDi1BXOZmp0U96iqvK7qfrs5gtlfEsPx/MJh+y9RvvywIDAQAB";
        var start: Instant? = null
        var end: Instant? = null
        val op = ImmutableAddUserOperation.builder()
                .user(userId)
                .publicKey(getKey(pubKey))
                .start(Optional.fromNullable<Instant>(start))
                .end(Optional.fromNullable<Instant>(end))
                .build()

        Doordeck.share(op, UUID.fromString("a8d81250-25f8-11e8-ba42-6986d3c6ca8e"), object: ShareCallback{
            override fun invalidAuthToken() {
                Toast.makeText(applicationContext, "invalid Auth", LENGTH_SHORT).show()
            }

            override fun shareSuccess() {
                Toast.makeText(applicationContext, "share Success", LENGTH_SHORT).show()
            }

            override fun shareFailed() {
                Toast.makeText(applicationContext, "share failed", LENGTH_SHORT).show()
            }

            override fun notAuthenticated() {
                Toast.makeText(applicationContext, "no auth", LENGTH_SHORT).show()
            }
        })
    }

    fun getKey(key: String): PublicKey? {
        try {
            val byteKey = Base64.decode(key.toByteArray(), Base64.DEFAULT)
            val X509publicKey = X509EncodedKeySpec(byteKey)
            val kf = KeyFactory.getInstance("RSA")

            return kf.generatePublic(X509publicKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun unlockWithQRCode() {
        // the ScanType is optional, by default it's set to NFC
        // the callback is optional too
        Doordeck.showUnlock(this, ScanType.QR, object : UnlockCallback {
            override fun invalidAuthToken() {
                Toast.makeText(applicationContext, "Invalid auth token", LENGTH_SHORT).show()

            }

            override fun notAuthenticated() {
                Toast.makeText(applicationContext, "Not authentificated", LENGTH_SHORT).show()
            }

            override fun unlockSuccess() {
                Toast.makeText(applicationContext, "Unlock Success", LENGTH_SHORT).show()
            }

            override fun unlockFailed() {
                Toast.makeText(applicationContext, "Unlock Failed", LENGTH_SHORT).show()
            }
        })
    }

    // suscribe to Doordeck.eventsObservable() that emits the events sent by the SDK
    private fun listenForEventsRx() {
        disposables.add(Doordeck.eventsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    Log.d("MainActivity", "event received : $event")
                }, Throwable::printStackTrace))
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

}
