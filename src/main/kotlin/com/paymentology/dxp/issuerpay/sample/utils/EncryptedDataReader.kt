package com.paymentology.dxp.issuerpay.sample.utils

import android.content.Context
import android.net.Uri
import com.paymentology.dxp.issuerpay.sample.dto.EncryptedData
import org.json.JSONObject

object EncryptedDataReader {
    private const val ENCRYPTED_DATA_FILE = "encrypted-data.json"

    fun readEncryptedDataFromAssets(context: Context): EncryptedData? {
        return runCatching {
            context.assets.open(ENCRYPTED_DATA_FILE).bufferedReader().use { reader ->
                parseEncryptedData(reader.readText())
            }
        }.getOrNull()
    }

    fun readEncryptedDataFromContent(context: Context, path: Uri): EncryptedData? {
        return runCatching {
            context.contentResolver.openInputStream(path)?.bufferedReader()?.use { reader ->
                parseEncryptedData(reader.readText())
            }
        }.getOrNull()
    }

    private fun parseEncryptedData(jsonContent: String): EncryptedData? {
        val json = JSONObject(jsonContent)
        val encryptedCardData = json.optString("encryptedCardData")
        val publicKeyFingerprint = json.optString("publicKeyFingerprint")
        val encryptedKey = json.optString("encryptedKey")
        val iv = json.optString("iv")
        if (
            encryptedCardData.isBlank() ||
            publicKeyFingerprint.isBlank() ||
            encryptedKey.isBlank() ||
            iv.isBlank()
        ) {
            return null
        }

        return EncryptedData(
            encryptedCardData = encryptedCardData,
            publicKeyFingerprint = publicKeyFingerprint,
            encryptedKey = encryptedKey,
            iv = iv
        )
    }
}
