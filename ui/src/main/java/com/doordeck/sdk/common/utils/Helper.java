package com.doordeck.sdk.common.utils;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Helper {

    public static JSONObject decode(String JWTEncoded) throws JSONException {
        String[] split = JWTEncoded.split("\\.");
        return getJson(split[1]);
    }

    private static JSONObject getJson(String strEncoded) throws JSONException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
        return new JSONObject(decoded);
    }

    public static boolean isUUID(String id){
        try {
            UUID ignored = UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static PublicKey getKey(String key){
        try{
            byte[] byteKey = Base64.decode(key.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static boolean checkIfTokenIsValid(String jwt) {
        try {
            JSONObject jwtClaims = decode(jwt);
            Date expiryDate = new Date(jwtClaims.getLong("exp") * 1000);
            return new Date().before(expiryDate);
        } catch (JSONException | NoSuchElementException e) {
            return false; // If we can't parse the token, its unlikely to be valid
        }
    }
}
