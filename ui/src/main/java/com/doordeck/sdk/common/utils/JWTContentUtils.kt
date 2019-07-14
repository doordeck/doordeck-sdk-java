package com.doordeck.sdk.common.utils

import android.util.Base64
import com.doordeck.sdk.common.models.JWTHeader
import com.doordeck.sdk.jackson.Jackson
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

internal object JWTContentUtils {

    /**
     * parse the JWT and extract the value into a JWTHeader object
     * @param JWT token of the user
     *  @return JWTHeader is generated, without any issue
     */
    fun getContentHeaderFromJson(jwt: String): JWTHeader? {

        try {
            val json = getJson(splitString(jwt))
            return Jackson.sharedObjectMapper().readValue(json, JWTHeader::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun splitString(jwt: String): String {
        try {
            return jwt.split("\\.".toRegex()).toTypedArray()[1]
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

}
