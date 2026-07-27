package com.paymentology.dxp.issuerpay.sample.dto

data class EncryptedData(
    val encryptedCardData: String,
    val publicKeyFingerprint: String,
    val encryptedKey: String,
    val iv: String
)
