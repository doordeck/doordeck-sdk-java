package com.doordeck.sdk.ui.unlock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.doordeck.sdk.R
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.verify.VerifyDeviceActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.android.synthetic.main.activity_unlock.*

internal class UnlockActivity : BaseActivity(), UnlockView {

    private var unlockPresenter: UnlockPresenter? = null
    private var locationPermissionShown = false
    private var googlePermissionShown = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        unlockPresenter = UnlockPresenter()
        tvDismiss.setOnClickListener { finishActivity() }
    }


    override fun showNoAccessGeoFence() {
        showAccessDeniedAnimation()
        unlock_status.setText(R.string.ACCESS_DENIED_GEOFENCE)
    }

    override fun updateLockName(name: String) {
        key_title.text = name
    }

    override fun setUnlocking() {
        unlock_status.text = getText(R.string.UNLOCKING)
    }

    override fun showGeoLoading() {
        unlock_status.setText(R.string.CHECKING_GEOFENCE)
    }


    override fun finishActivity() {
        finish()
    }


    private fun resetAnimation() {

        logo_spinner.visibility = View.VISIBLE
        arrow_image.scaleX = 0f
        arrow_image.scaleY = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            arrow_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unlock_success))
        } else {
            arrow_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unlock_success))
        }
        unlock_status.setText(R.string.UNLOCKING)
    }

    override fun unlockSuccess() {
        showUnlockAnimation()
        unlockPresenter?.setFinishTimer()
    }


    private fun showUnlockAnimation() {

        tvDismiss.setBackgroundColor(ContextCompat.getColor(this, R.color.black_transp))
        rlContent.setBackgroundColor(ContextCompat.getColor(this, R.color.success))
        key_title.setTextColor(Color.WHITE)
        arrow_image.setColorFilter(ContextCompat.getColor(this, R.color.success), PorterDuff.Mode.SRC_IN)

        logo_spinner.alpha = 0f
        arrow_image.animate().scaleX(0.4f).scaleY(0.4f).alpha(1.0f).setInterpolator(OvershootInterpolator()).setDuration(300).startDelay = 200
        arrow_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unlock_success))
        val animation = arrow_image.drawable
        if (animation is Animatable) {
            (animation as Animatable).start()
        }
        unlock_status.setText(R.string.UNLOCKED)
        unlock_status.setTextColor(ContextCompat.getColor(this, R.color.ddColorTextLight))
    }

    override fun showAccessDenied() {
        showAccessDeniedAnimation()
        unlock_status.text = ""
    }

    override fun notValidTileId() {
        Toast.makeText(this, getString(R.string.tile_id_not_valid), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun displayVerificationView() {
        VerifyDeviceActivity.start(this)
    }

    private fun showAccessDeniedAnimation() {

        tvDismiss.setBackgroundColor(ContextCompat.getColor(this, R.color.black_transp))
        rlContent.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
        key_title.setTextColor(Color.WHITE)
        arrow_image.setColorFilter(ContextCompat.getColor(this, R.color.error), PorterDuff.Mode.SRC_IN)
        val duration: Long = 600
        logo_spinner.alpha = 0f

        arrow_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_access_denied))
        arrow_image.animate().scaleX(0.4f).scaleY(0.4f).alpha(1.0f).setInterpolator(OvershootInterpolator()).duration = duration
        val animation = arrow_image.drawable
        if (animation is Animatable) {
            (animation as Animatable).start()
        }
        key_title.setText(R.string.ACCESS_DENIED)
        unlock_status.setTextColor(Color.WHITE)

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
                            .setPositiveButton(R.string.OK) { _, i ->
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

    override fun onBackPressed() {
        finish()
    }

    public override fun onStart() {
        super.onStart()
        unlockPresenter?.onStart(this)
    }

    public override fun onResume() {
        super.onResume()
        unlockPresenter?.init(intent.extras?.getString(TILE_ID))
        resetAnimation()
    }

    public override fun onStop() {
        super.onStop()
        unlockPresenter?.onStop()
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

        private const val TILE_ID = "tile_id"

        fun start(context: Context, id: String) {
            val starter = Intent(context, UnlockActivity::class.java)
            starter.putExtra(TILE_ID, id)
            context.startActivity(starter)
        }
    }

}
