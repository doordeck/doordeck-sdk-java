package com.doordeck.sdk.ui;


import android.os.Bundle;

import com.doordeck.sdk.R;
import com.doordeck.sdk.common.contstants.Constants;
import com.doordeck.sdk.common.utils.PreferencesManager;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity implements BaseView{

    protected boolean active = false;
    protected boolean lightTheme = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        PreferencesManager.initializeInstance(this);
        setTheme(getCurrentTheme());
        super.onCreate(savedInstanceState);
        active = true;
    }

    private int getCurrentTheme() {
        lightTheme = PreferencesManager.getInstance().getBoolean(Constants.prefs.LIGHT_THEME);
        if (lightTheme) {
            return R.style.ddlightTheme;
        } else {
            return R.style.ddDarkTheme;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public boolean isActive(){
        return active;
    }
}
