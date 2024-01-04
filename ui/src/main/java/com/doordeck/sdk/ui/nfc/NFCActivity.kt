package com.doordeck.sdk.ui.nfc


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.widget.Toast
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_NFC
import com.github.doordeck.ui.R
import com.github.doordeck.ui.databinding.ActivityNfcBinding


/**
 * Launch the activity to scan the tile using NFC
 */
internal class NFCActivity : BaseActivity(), NFCView {

    private lateinit var nfcPresenter: NFCPresenter

    private lateinit var binding: ActivityNfcBinding

    private var flagWentToBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNfcEnabled()
        nfcPresenter = NFCPresenter()
        setupListeners()
    }

    private fun setupListeners() {
        binding.tvDismiss.setOnClickListener { finish() }
    }


    private fun checkNfcEnabled() {
        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter = manager.defaultAdapter
        if (adapter != null && !adapter.isEnabled) {
            Toast.makeText(applicationContext, getString(R.string.nfc_not_enabled_message), Toast.LENGTH_LONG).show()
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

    override fun onPause() {
        super.onPause()

        disableListeningHereReEnableNFCSystemReading()
    }

    override fun onResume() {
        super.onResume()

        listenNfcDisableSystem()

        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // Yes, Activity start via Beam...  wonder if we should pass a flag indicating Beam?
            nfcPresenter.processNFCData(intent)
        } else if (flagWentToBackground) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        nfcPresenter.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        nfcPresenter.onStop()
        // This is also called when we move to another screen, but onResume won't be called as we're leaving this activity
        // Also this won't be triggered when we get asked for a permission
        flagWentToBackground = true
    }


    override fun unlockFromTileId(tileId: String) {
        val uuid = Uri.parse(tileId).lastPathSegment ?: ""

        intent.action = ""
        UnlockActivity.start(this, uuid, comingFrom = COMING_FROM_NFC)
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Reading NFC on activity.
     * Disables Android system reading
     * Methods:
     */

    private val nfcOnActivityPendingIntent by lazy {
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    private val nfcOnActivityIntentFilters by lazy {
        arrayOf(
            // Old method, needs to be migrated at some point
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                addDataType("text/plain")
            },
            // New method
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                addDataScheme(getString(R.string.nfc_uri_scheme))
                addDataAuthority(getString(R.string.nfc_uri_host), null)
            }
        )
    }

    private fun listenNfcDisableSystem() {
        NfcAdapter
            .getDefaultAdapter(this)
            ?.enableForegroundDispatch(
                this@NFCActivity,
                nfcOnActivityPendingIntent,
                nfcOnActivityIntentFilters,
                null
            )
    }

    private fun disableListeningHereReEnableNFCSystemReading() {
        NfcAdapter
            .getDefaultAdapter(this)
            ?.disableForegroundDispatch(this@NFCActivity)
    }

    /**
     * End of disabling Android NFC system reading
     */

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, NFCActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }

}
