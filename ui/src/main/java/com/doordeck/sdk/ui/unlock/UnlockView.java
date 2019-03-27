package com.doordeck.sdk.ui.unlock;

import com.doordeck.sdk.ui.BaseView;

public interface UnlockView extends BaseView {
    void showAccessDenied();

    void showNoAccessGeoFence();

    void unlockSuccess();

    void updateLockName(String name);

    void setUnlocking();

    void showGeoLoading();

    void checkGoogleApiPermissions();

    void finishActivity();
}
