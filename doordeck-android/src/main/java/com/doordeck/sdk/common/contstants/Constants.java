package com.doordeck.sdk.common.contstants;

public class Constants {

    public interface urls {
        String ORIGIN_API = "https://app.doordeck.com";
    }

    public interface user {
        String DISPLAY_NAME = "displayName";
    }

    public interface prefs {
        String TOKEN = "authToken";
        String REFRESHTOKEN = "refreshToken";
        String PRIKEY = "privateKey";
        String PUBKEY = "publicKey";
        String SAVED_LOCKS = "savedLocks";
        String LIGHT_THEME = "lightTheme";
        String EPHEMERAL_KEY = "ephemeralKey";
    }

    public interface intent {
        String BUTTON_UNLOCK = "buttonUnlock";
        String WIDGET_UNLOCK = "widgetUnlock";
        String QR_UNLOCK = "qrUnlock";
    }

    public interface lock{
        String ADMIN = "ADMIN";
        String ROLE = "role";
        String NAME = "name";
        String ID = "id";
        String COLOR = "colour";
        String STATE = "state";
        String CONNECTED = "connected";
        String FAVOURITE = "favourite";
        String SETTINGS = "settings";
        String UNLOCK_TIME = "unlockTime";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String RADIUS = "radius";
        String ACCURACY = "accuracy";

    }
    public interface permission {
        int LOCATION = 99;
        int CAMERA = 98;
        int BLE = 100;
        int GOOGLE_SAVE = 97;
    }
}
