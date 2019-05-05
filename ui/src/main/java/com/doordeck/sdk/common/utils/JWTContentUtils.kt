package com.doordeck.sdk.common.utils

import android.util.Base64
import com.doordeck.sdk.common.models.JWTHeader
import com.doordeck.sdk.jackson.Jackson
import com.google.common.base.Splitter
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
            val json = getJson(Splitter.on('.').splitToList(jwt)[1])
            return Jackson.sharedObjectMapper().readValue(json, JWTHeader::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

}
