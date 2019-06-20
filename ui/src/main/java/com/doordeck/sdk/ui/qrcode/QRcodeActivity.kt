package com.doordeck.sdk.ui.qrcode


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.doordeck.sdk.R
import com.doordeck.sdk.common.events.EventsManager
import com.doordeck.sdk.common.models.EventAction
import com.doordeck.sdk.ui.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_qr_scan.*


/**
 * Launch the activity to scan with the QR Code
 */
internal class QRcodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)
        setupListeners()
    }

    private fun setupListeners() {
        tvDismiss.setOnClickListener { finish() }
    }

    // check if the user has grandted the camera permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA)
                qr.start()
        }
    }

    public override fun onStart() {
        super.onStart()
        qr.start()
    }


    public override fun onResume() {
        super.onResume()
        qr.start()
    }

    override fun onPause() {
        super.onPause()
        qr.pause()
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
