package com.doordeck.sdk.ui.unlock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;

import androidx.annotation.NonNull;

import butterknife.ButterKnife;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.doordeck.sdk.Doordeck;
import com.doordeck.sdk.R2;
import com.doordeck.sdk.common.contstants.Constants;
import com.doordeck.sdk.common.utils.PreferencesManager;
import com.doordeck.sdk.ui.BaseActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.OnClick;

import static com.doordeck.sdk.common.contstants.Constants.prefs.REFRESHTOKEN;
import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;

public class UnlockActivity extends BaseActivity implements UnlockView {
//public class UnlockActivity extends BaseActivity implements UnlockView, Doordeck.AuthenticateCallback {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R2.id.key_title)
    TextView keyTV;

    @BindView(R2.id.unlock_status)
    TextView statusTV;

    @BindView(R2.id.logo_spinner)
    ProgressBar spinnerPB;

    @BindView(R2.id.lock_bkg)
    ImageView circleBkgIV;

    @BindView(R2.id.arrow_image)
    ImageView arrowIV;

    @BindView(R2.id.dismissBtn)
    Button dismissBtn;

    @BindView(R2.id.fav_button)
    FloatingActionButton favBtn;

    @OnClick(R2.id.dismissBtn)
    public void onDismissClicked() {
        finish();
    }

    @OnClick(R2.id.fav_button)
    public void onFavButtonClicked() {
        if (theme == R2.style.ddlightTheme) {
            PreferencesManager.getInstance().setInt(Constants.prefs.LIGHT_THEME, R2.style.ddDarkTheme);
        } else {
            PreferencesManager.getInstance().setInt(Constants.prefs.LIGHT_THEME, R2.style.ddlightTheme);
        }
    }

    private UnlockPresenter unlockPresenter;
    private boolean locationPermissionShown = false;
    private boolean googlePermissionShown = false;
    private int theme;


    @Override
    public void onCreate(Bundle savedInstanceState) {
//
//        Doordeck.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R2.layout.activity_unlock);
        ButterKnife.bind(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        if(unlockPresenter == null) {
            unlockPresenter = new UnlockPresenter(this);
        }
        resetAnimation();



    }

//    @Override
    public void authenticationSuccess() {
        if(unlockPresenter == null) {
            unlockPresenter = new UnlockPresenter(this);
        }
        unlockPresenter.handleIntent(getIntent());
    }

//    @Override
    public void authenticationFail(String error) {
        showAuthError(error);
    }

    private void showAuthError(String error) {
        statusTV.setText(error);
        showAccessDeniedAnimation();
    }




    @Override
    public void showNoAccessGeoFence() {
        showAccessDeniedAnimation();
        statusTV.setText(R2.string.ACCESS_DENIED_GEOFENCE);
    }

    @Override
    public void updateLockName(String name) {
        keyTV.setText(name);
    }

    @Override
    public void setUnlocking() {
        statusTV.setText(getText(R2.string.UNLOCKING));
    }

    @Override
    public void showGeoLoading() {
        statusTV.setText(R2.string.CHECKING_GEOFENCE);
    }



    @Override
    public void finishActivity() {
        finish();
    }


    private void resetAnimation() {

        spinnerPB.setVisibility(View.VISIBLE);
        arrowIV.setScaleX(0f);
        arrowIV.setScaleY(0f);
        circleBkgIV.setScaleX(1f);
        circleBkgIV.setScaleY(1f);
        Drawable circle = circleBkgIV.getDrawable();
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R2.attr.ddColorAccent, typedValue, true);
        int colorAccent = typedValue.data;
        if (circle instanceof ShapeDrawable) {
            ((ShapeDrawable)circle).getPaint().setColor(colorAccent);
        } else if (circle instanceof GradientDrawable) {
            ((GradientDrawable)circle).setColor(colorAccent);
        } else if (circle instanceof ColorDrawable) {
            ((ColorDrawable)circle).setColor(colorAccent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_unlock_success));
        }
        else{
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_unlock_success));
        }
        statusTV.setText(R2.string.UNLOCKING);
    }

    @Override
    public void unlockSuccess() {
        showUnlockAnimation();
        unlockPresenter.setFinishTimer();
    }


    public void showUnlockAnimation(){

        spinnerPB.setAlpha(0);
        Drawable circle = circleBkgIV.getDrawable();
        if (circle instanceof ShapeDrawable) {
            ((ShapeDrawable)circle).getPaint().setColor(ContextCompat.getColor(this,R2.color.success));
        } else if (circle instanceof GradientDrawable) {
            ((GradientDrawable)circle).setColor(ContextCompat.getColor(this,R2.color.success));
        } else if (circle instanceof ColorDrawable) {
            ((ColorDrawable)circle).setColor(ContextCompat.getColor(this,R2.color.success));
        }
        circleBkgIV.animate().scaleX(25f).scaleY(25f).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500);
        arrowIV.animate().scaleX(1f).scaleY(1f).alpha(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(300).setStartDelay(200);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_unlock_success));
        }
        else{
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_unlock_success));
        }
        Drawable animation = arrowIV.getDrawable();
        if (animation instanceof Animatable) {
            ((Animatable) animation).start();
        }
        statusTV.setText(R2.string.UNLOCKED);
        statusTV.setTextColor(ContextCompat.getColor(this, R2.color.ddColorTextLight));
        favBtn.setAlpha(0.0f);
        favBtn.setVisibility(View.VISIBLE);
        favBtn.animate().alpha(1.0f).setDuration(200);
    }

    @Override
    public void showAccessDenied() {
        showAccessDeniedAnimation();
        statusTV.setText("");
    }

    public void showAccessDeniedAnimation(){

        long duration = 600;
        spinnerPB.setAlpha(0);

        Drawable circle = circleBkgIV.getDrawable();
        if (circle instanceof ShapeDrawable) {
            ((ShapeDrawable)circle).getPaint().setColor(ContextCompat.getColor(this,R2.color.error));
        } else if (circle instanceof GradientDrawable) {
            ((GradientDrawable)circle).setColor(ContextCompat.getColor(this,R2.color.error));
        } else if (circle instanceof ColorDrawable) {
            ((ColorDrawable)circle).setColor(ContextCompat.getColor(this,R2.color.error));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_access_denied));
        }
        else{
            arrowIV.setImageDrawable(ContextCompat.getDrawable(this,R2.drawable.ic_access_denied));
        }
        circleBkgIV.animate().scaleX(25f).scaleY(25f).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500);
        arrowIV.animate().scaleX(1.2f).scaleY(1.2f).alpha(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(duration);
        Drawable animation = arrowIV.getDrawable();
        if (animation instanceof Animatable) {
            ((Animatable) animation).start();
        }
        keyTV.setText(R2.string.ACCESS_DENIED);
        statusTV.setTextColor(Color.WHITE);

    }

    @Override
    public void checkGoogleApiPermissions() {
        if (checkLocationPermission()){

            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // check if location is enabled
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            // location is enabled
            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    unlockPresenter.checkGeofence();
                }
            });

            // location is disabled
            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // LocationObject settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            if(!googlePermissionShown) {
                                googlePermissionShown = true;
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(UnlockActivity.this,
                                        Constants.permission.LOCATION);
                            }
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                            sendEx.printStackTrace();
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                if(!locationPermissionShown) {
                    locationPermissionShown = true;
                    new AlertDialog.Builder(this)
                            .setTitle(R2.string.LOCATION_PERMISSION_TITLE)
                            .setMessage(R2.string.LOCATION_PERMISSION_TEXT)
                            .setPositiveButton(R2.string.OK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(UnlockActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            Constants.permission.LOCATION);
                                }
                            })
                            .create()
                            .show();
                }

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.permission.LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }



}
