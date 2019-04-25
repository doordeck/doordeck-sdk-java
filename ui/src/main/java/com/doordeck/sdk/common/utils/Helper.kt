package com.doordeck.sdk.common.utils

import android.util.Base64
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.*

internal object Helper {

    fun getBodyFromJson(JWTEncoded: String?): JSONObject {

        JWTEncoded?.let {
            try {
                val split = JWTEncoded.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                LOG.d("JWT_DECODED", "Header: " + getJson(split[0]))
                return JSONObject(getJson(split[1]))
            } catch (e: UnsupportedEncodingException) {
                //Error
            }
        }
        return JSONObject()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    fun isUUID(id: String): Boolean {
        return try {
            UUID.fromString(id)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    }

    fun distance(lat1: Double, lat2: Double, lon1: Double,
                 lon2: Double, el1: Double, el2: Double): Double {
        val R = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R.toDouble() * c * 1000.0 // convert to meters

        val height = el1 - el2

        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)

        return Math.sqrt(distance)
    }
}
