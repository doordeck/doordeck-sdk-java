package com.doordeck.sdk.ui.qrcode


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_QR_SCAN
import com.github.doordeck.ui.databinding.ActivityQrScanBinding
import androidx.core.net.toUri


/**
 * Launch the activity to scan with the QR Code
 */
internal class QRcodeActivity : BaseActivity() {

    private lateinit var binding: ActivityQrScanBinding

    private var flagWentToBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.tvDismiss.setOnClickListener { finish() }
    }

    // check if the user has granted the camera permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA)
                binding.qr.start()
        }
    }

    public override fun onStart() {
        super.onStart()
        binding.qr.start()
    }


    public override fun onResume() {
        super.onResume()
        binding.qr.start()

        if (Intent.ACTION_VIEW == intent.action) {
            val uuid = intent.dataString?.toUri()?.lastPathSegment
            if (uuid != null) {
                UnlockActivity.start(this, uuid, comingFrom = COMING_FROM_QR_SCAN)
            }
        } else if (flagWentToBackground) {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.qr.pause()
    }

    override fun onStop() {
        super.onStop()
        // This is also called when we move to another screen, but onResume won't be called as we're leaving this activity
        // Also this won't be triggered when we get asked for a permission
        flagWentToBackground = true
    }

    companion object {

        const val CAMERA = 98

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, QRcodeActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }

}
