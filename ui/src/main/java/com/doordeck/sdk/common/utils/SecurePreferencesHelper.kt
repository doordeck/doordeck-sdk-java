package com.doordeck.sdk.common.utils

import android.content.Context
import de.adorsys.android.securestoragelibrary.SecurePreferences

object SecurePreferencesHelper {
    private const val chunkSize = 240

    private fun getNumberOfChunksKey(key: String) = "${key}_numberOfChunks"

    fun setLongStringValue(context: Context, key: String, value: String) {
        val chunks = value.chunked(chunkSize)

        SecurePreferences.setValue(getNumberOfChunksKey(key), chunks.size, context)

        chunks.forEachIndexed { index, chunk ->
            SecurePreferences.setValue("$key$index", chunk, context)
        }
    }

    fun getLongStringValue(context: Context, key: String): String? {
        val numberOfChunks = SecurePreferences.getIntValue(getNumberOfChunksKey(key), context, 0)

        if (numberOfChunks == 0) {
            return null
        }

        return (0 until numberOfChunks)
                .map { index ->
                    val string = SecurePreferences.getStringValue( "$key$index", context, null) ?: run {
                        return null
                    }

                    string
                }.reduce { accumulator, chunk -> accumulator + chunk }
    }

    fun removeLongStringValue(context: Context, key: String) {
        val numberOfChunks = SecurePreferences.getIntValue(getNumberOfChunksKey(key), context, 0)

        (0 until numberOfChunks).map { SecurePreferences.removeValue("$key$it", context) }
        SecurePreferences.removeValue(getNumberOfChunksKey(key), context)
    }

    fun containsLongStringValue(context: Context, key: String): Boolean {
        val defaultInt = Int.MAX_VALUE
        return SecurePreferences.getIntValue(getNumberOfChunksKey(key), context, defaultInt) == defaultInt
    }
}