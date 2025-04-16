package com.doordeck.doordecksdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doordeck.doordecksdk.databinding.ActivityMainBinding
import com.doordeck.sdk.common.manager.AuthStatus
import com.doordeck.sdk.common.manager.Doordeck
import com.doordeck.sdk.common.manager.ScanType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Doordeck.initialize(applicationContext, true)
        Doordeck.setToken(getString(R.string.doordeck_api_key))

        listenToEvents()

        binding.nfc.setOnClickListener {
            if (needsVerification()) {
                return@setOnClickListener
            }
            Doordeck.showUnlock(this)
        }
        binding.qrcode.setOnClickListener {
            if (needsVerification()) {
                return@setOnClickListener
            }
            unlockWithQRCode()
        }
        binding.uuid.setOnClickListener {
            if (needsVerification()) {
                return@setOnClickListener
            }
            unlockTileID()
        }
    }

    private fun unlockWithQRCode() {
        // the ScanType is optional, by default it's set to NFC
        // the callback is optional too
        Doordeck.showUnlock(this, ScanType.QR)
    }

    private fun unlockTileID() {
        binding.uuidText.error = null
        try {
            val uuid = binding.uuidText.text.toString()
            unlockTileID(uuid)
        } catch (illegalStateException: IllegalStateException) {
            binding.uuidText.error = illegalStateException.message
        }
    }

    private fun unlockTileID(tileID: String) {
        Doordeck.unlockTileID(
            context = this,
            tileId = tileID,
        )
    }

    private fun needsVerification(): Boolean {
        if (Doordeck.authStatus == AuthStatus.TWO_FACTOR_AUTH_NEEDED) {
            Doordeck.showVerificationScreen(this)
            return true
        } else {
            return false
        }
    }

    private fun listenToEvents() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            Doordeck.eventsFlow().collect {
                println(it)
            }
        }
    }

}
