package com.doordeck.sdk.common.utils;

import android.util.Base64;
import android.util.Log;

import com.doordeck.sdk.core.dto.Operation;
import com.doordeck.sdk.jwt.Claims;
import com.doordeck.sdk.jwt.Header;
import com.doordeck.sdk.jwt.ImmutableClaims;
import com.doordeck.sdk.jwt.ImmutableHeader;
import com.doordeck.sdk.jwt.SupportedAlgorithm;
import com.doordeck.sdk.jwt.signer.RSASigner;

import org.joda.time.Instant;
import org.json.JSONException;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

import static com.doordeck.sdk.common.contstants.Constants.prefs.PRIKEY;
import static com.doordeck.sdk.common.contstants.Constants.prefs.TOKEN;

public class JWTUtil {

    public static String signJWT(String deviceId, Instant expiry, Operation operation) throws NoSuchAlgorithmException, IOException {
        String key = PreferencesManager.getInstance().getString(PRIKEY);
        byte[] pKey = Base64.decode(key.getBytes(), Base64.DEFAULT);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pKey);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            String issuer = Helper.decode(PreferencesManager.getInstance().getString(TOKEN)).getString("sub");

            Claims claims = ImmutableClaims.builder()
                    .deviceId(UUID.fromString(deviceId))
                    .userId(UUID.fromString(issuer))
                    .operation(operation)
                    .expiresAt(expiry)
                    .build();
            Header header = ImmutableHeader.builder()
                    .algorithm(SupportedAlgorithm.RS256)
                    .build();

            Log.d(JWTUtil.class.getSimpleName(), "JTI: " + claims.uniqueIdentifier());

            return new RSASigner().sign(header, claims, privateKey);
        } catch (JSONException | InvalidKeySpecException e) {
            Log.e(JWTUtil.class.getSimpleName(), "Unable to sign JWT request", e);
            throw new IOException(e);
        }
    }
}
