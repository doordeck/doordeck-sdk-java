package com.doordeck.sdk.ui.nfc


import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.widget.Toast
import com.doordeck.sdk.R
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_nfc.*


/**
 * Launch the activity to scan the tile using NFC
 */
internal class NFCActivity : BaseActivity(), NFCView {

    private lateinit var nfcPresenter: NFCPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        checkNfcEnabled()
        nfcPresenter = NFCPresenter()
        setupListeners()
    }

    private fun setupListeners() {
        tvDismiss.setOnClickListener { finish() }

        // listen to the CLOSE_NFC_VIEW event to close the activity from outside this view
        disposables.add(EventsManager.eventsObservable()
                .subscribeOn(Schedulers.io())
                .filter { it.event == EventAction.CLOSE_NFC_VIEW }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ finish() }, Throwable::printStackTrace))
    }


    private fun checkNfcEnabled() {
        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter = manager.defaultAdapter
        if (adapter != null && !adapter.isEnabled) {
            Toast.makeText(applicationContext, getString(R.string.nfc_not_enabled_message), Toast.LENGTH_LONG).show();
            startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // We scanned an NFC Tag.
            nfcPresenter.processNFCData(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // Yes, Activity start via Beam...  wonder if we should pass a flag indicating Beam?
            nfcPresenter.processNFCData(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        nfcPresenter.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        nfcPresenter.onStop()
    }


    override fun unlockFromTileId(tileId: String) {
        intent.action = ""
        UnlockActivity.start(this, tileId)
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, NFCActivity::class.java)
            context.startActivity(starter)
        }
    }

}
