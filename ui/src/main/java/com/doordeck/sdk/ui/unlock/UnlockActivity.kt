package com.doordeck.sdk.ui.unlock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.github.doordeck.ui.R
import com.doordeck.sdk.common.manager.Doordeck
import com.github.doordeck.ui.databinding.ActivityUnlockBinding
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.jackson.Jackson
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.nfc.NFCActivity
import com.doordeck.sdk.ui.qrcode.QRcodeActivity
import com.doordeck.sdk.ui.showlistofdevicestounlock.ShowListOfDevicesToUnlockActivity
import com.doordeck.sdk.ui.verify.VerifyDeviceActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.max

// screen responsible to display the status of the unlock process
internal class UnlockActivity : BaseActivity(), UnlockView {

    private var unlockPresenter: UnlockPresenter? = null
    private var locationPermissionShown = false
    private var googlePermissionShown = false
    private var canceledVerify = false
    // Using this to avoid entering on onResume after a beaming.
    //
    // There can be a situation when the user beams, goes to see result and beams
    // again while/after computing the result without clicking "Dismiss"
    private var unlockFinished = false

    private lateinit var binding: ActivityUnlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        unlockPresenter = UnlockPresenter()
        binding.tvDismiss.setOnClickListener { back() }
    }

    private fun back() {
        finish()
        when (comingFrom) {
            COMING_FROM_DIRECT_UNLOCK -> {
                // NO-OP, just finish ☝️
            }

            COMING_FROM_QR_SCAN -> QRcodeActivity.start(this)
            COMING_FROM_NFC -> NFCActivity.start(this)
        }

        unlockFinished = false
    }

    override fun showNoAccessGeoFence() {
        showAccessDeniedAnimation()
        binding.unlockStatus.setText(R.string.ACCESS_DENIED_GEOFENCE)

        unlockFinished = true
    }

    override fun updateLockName(name: String) {
        binding.keyTitle.text = name
    }

    override fun setUnlocking() {
        binding.unlockStatus.text = getText(R.string.UNLOCKING)
    }

    override fun showGeoLoading() {
        binding.unlockStatus.setText(R.string.CHECKING_GEOFENCE)
    }


    override fun finishActivity() {
        back()
    }


    private fun resetAnimation() {
        binding.circleBack.scaleX = 0f
        binding.circleBack.scaleY = 0f

        binding.keyTitle.alpha = 0f

        val animated = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_unlock_success)
        val animation = binding.lockImage.drawable
        if (animation is Animatable) {
            (animation as Animatable).stop()
        }
        binding.lockImage.setImageDrawable(null)
        binding.lockImage.setImageDrawable(animated)
        binding.logoSpinner.visibility = View.VISIBLE
        binding.unlockStatus.setText(R.string.UNLOCKING)
    }

    private fun showDelayTimer(delay: Double) {
        val animated = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_unlock_success_blank)
        val animation = binding.lockImage.drawable
        if (animation is Animatable) {
            (animation as Animatable).stop()
        }
        binding.lockImage.setImageDrawable(null)
        binding.lockImage.setImageDrawable(animated)

        binding.unlockStatus.text = getString(R.string.Please_Wait)

        GlobalScope.launch(Dispatchers.Main) {
            val tickSeconds = 1
            val totalSeconds = max(TimeUnit.SECONDS.toSeconds(delay.toLong()), tickSeconds.toLong())
            for (second in totalSeconds downTo tickSeconds) {
                binding.delayLockTimeText.text = second.toString()
                delay(1000)
            }

            binding.delayLockTimeText.text = null

            // Finish with the timer and show the unlock
            showUnlockAnimation()
            unlockPresenter?.setFinishTimer()
        }
    }

    override fun unlockSuccessWithDelay(delayOfDevice: Double) {
        showDelayTimer(delayOfDevice)
    }

    override fun unlockSuccess() {
        showUnlockAnimation()
        unlockPresenter?.setFinishTimer()
    }


    private fun showUnlockAnimation() {

        binding.tvDismiss.setBackgroundColor(ContextCompat.getColor(this, R.color.black_transp))
        binding.circleBack.setColorFilter(ContextCompat.getColor(this, R.color.success), PorterDuff.Mode.SRC_IN)
        binding.circleBack.animate().alpha(1f).scaleX(13f).scaleY(13f).setInterpolator(AccelerateInterpolator()).setDuration(500)
        binding.logoSpinner.alpha = 0f
        val animated = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_unlock_success)
        binding.lockImage.setImageDrawable(animated)
        val animation = binding.lockImage.drawable
        if (animation is Animatable) {
            (animation as Animatable).start()
        }
        binding.unlockStatus.setText(R.string.UNLOCKED)
        binding.keyTitle.animate().alpha(1f).translationY(150f).setInterpolator(OvershootInterpolator()).setDuration(500).startDelay = 500
    }

    override fun showAccessDenied() {
        showAccessDeniedAnimation()
        binding.unlockStatus.text = ""

        unlockFinished = true
    }

    override fun notValidTileId() {
        Toast.makeText(this, getString(R.string.tile_id_not_valid), Toast.LENGTH_LONG).show()
        back()
    }

    override fun noUserLoggedIn() {
        showAccessDeniedAnimation()
        binding.unlockStatus.text = getString(R.string.no_user_logged_in)

        unlockFinished = true
    }

    override fun goToDevices(devices: List<Device>) {
        ShowListOfDevicesToUnlockActivity.start(this, devices)
    }

    override fun getDefaultLockColours(): Array<String> {
        return resources.getStringArray(R.array.lock_colours)
    }

    override fun displayVerificationView() {
        if (!canceledVerify) {
            canceledVerify = true
            VerifyDeviceActivity.start(this)
        } else {
            back()
        }
    }

    private fun showAccessDeniedAnimation() {
        binding.tvDismiss.setBackgroundColor(ContextCompat.getColor(this, R.color.black_transp))
        binding.circleBack.setColorFilter(ContextCompat.getColor(this, R.color.error), PorterDuff.Mode.SRC_IN)
        binding.circleBack.animate().alpha(1f).scaleX(13f).scaleY(13f).setInterpolator(AccelerateInterpolator()).setDuration(500)
        binding.logoSpinner.alpha = 0f
        val animated = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_unlock_fail)
        binding.lockImage.setImageDrawable(animated)
        val animation = binding.lockImage.drawable
        if (animation is Animatable) {
            (animation as Animatable).start()
        }
        binding.keyTitle.setText(R.string.ACCESS_DENIED)
        binding.keyTitle.animate().alpha(1f).translationY(150f).setInterpolator(OvershootInterpolator()).setDuration(500).startDelay = 500

        unlockFinished = true
    }

    override fun checkGoogleApiPermissions() {
        if (checkLocationPermission()) {

            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            // check if location is enabled
            val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
            val client = LocationServices.getSettingsClient(this)
            val task = client.checkLocationSettings(builder.build())

            // location is enabled
            task.addOnSuccessListener(this) { unlockPresenter?.checkGeofence() }

            // location is disabled
            task.addOnFailureListener(this) { e ->
                if (e is ResolvableApiException) {
                    // LocationObject settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        if (!googlePermissionShown) {
                            googlePermissionShown = true
                            e.startResolutionForResult(this@UnlockActivity,
                                    LOCATION)
                        }
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                        sendEx.printStackTrace()
                    }

                } else {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                if (!locationPermissionShown) {
                    locationPermissionShown = true
                    AlertDialog.Builder(this)
                            .setTitle(R.string.LOCATION_PERMISSION_TITLE)
                            .setMessage(R.string.LOCATION_PERMISSION_TEXT)
                            .setPositiveButton(R.string.OK) { _, _ ->
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(this@UnlockActivity,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        LOCATION)
                            }
                            .create()
                            .show()
                }

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION)
            }
            return false
        } else {
            return true
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        back()
    }

    public override fun onStart() {
        super.onStart()
        unlockPresenter?.onStart(this)
    }

    public override fun onResume() {
        super.onResume()
        if (unlockFinished) {
            return
        }

        resetAnimation()
        if(!Doordeck.hasUserLoggedIn(this)) {
            noUserLoggedIn()
            return
        }
        if (tileId != null) {
            unlockPresenter?.init(tileId)
        } else if (deviceJson != null)  {
            val om = Jackson.sharedObjectMapper()
            val deviceToUnlock = om.readValue(deviceJson, Device::class.java)
            unlockPresenter?.init(deviceToUnlock)
        }

    }

    public override fun onPause() {
        super.onPause()
        unlockPresenter?.onStop()
        unlockPresenter = null
    }

    public override fun onStop() {
        super.onStop()
        unlockPresenter?.onStop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unlockPresenter = null
    }

    companion object {

        private const val LOCATION = 99

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val TILE_ID = "TILE_ID"
        private const val DEVICE = "DEVICE"


        /**
         * Using this from the QR/NFC viewer, so we finish the activity which is the previous one,
         * if it's an activity
         */
        fun start(context: Context, tileId: String, comingFrom: String) {
            val starter = Intent(context, UnlockActivity::class.java)
            starter.putExtra(TILE_ID, tileId)
            starter.putExtra(COMING_FROM_KEY, comingFrom)
            if (comingFrom != COMING_FROM_DIRECT_UNLOCK) {
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(starter)
        }
        fun start(context: Context, device: Device, comingFrom: String) {
            val starter = Intent(context, UnlockActivity::class.java)
            val om = Jackson.sharedObjectMapper()
            starter.putExtra(DEVICE, om.writeValueAsString(device))
            starter.putExtra(COMING_FROM_KEY, comingFrom)
            if (comingFrom != COMING_FROM_DIRECT_UNLOCK) {
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(starter)
        }

        private const val COMING_FROM_KEY = "comingFrom"
        const val COMING_FROM_DIRECT_UNLOCK = "comingFromDirectUnlock"
        const val COMING_FROM_QR_SCAN = "comingFromQRScan"
        const val COMING_FROM_NFC = "comingFromNFC"
    }

    private val comingFrom: String?
        get() = intent.extras?.getString(COMING_FROM_KEY)

    private val tileId: String?
        get() = intent.extras?.getString(TILE_ID)

    private val deviceJson: String?
        get() = intent.extras?.getString(DEVICE)

}
