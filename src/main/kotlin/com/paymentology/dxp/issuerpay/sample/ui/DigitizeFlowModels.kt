package com.paymentology.dxp.issuerpay.sample.ui

import androidx.annotation.StringRes
import com.paymentology.dxp.issuerpay.sample.R
import java.util.Calendar
import kotlin.random.Random

enum class DigitizationOption(@StringRes val labelRes: Int) {
    Normal(R.string.ui_digitization_option_normal),
    YellowFlow(R.string.ui_digitization_option_yellow_flow),
    RedFlow(R.string.ui_digitization_option_red_flow)
}

enum class PaymentNetwork(@StringRes val labelRes: Int) {
    Mastercard(R.string.ui_payment_network_mastercard),
    Visa(R.string.ui_payment_network_visa)
}

enum class DigitizationMethod(@StringRes val labelRes: Int) {
    PAN(R.string.ui_digitization_method_pan),
    CARD_ID(R.string.ui_digitization_method_card_id),
    ENCRYPTED_PAN(R.string.ui_digitization_method_encrypted_pan)
}

fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR) % 100

fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

fun getGeneratedCardholderName(
    option: DigitizationOption,
    currentCardholderName: String
): String {
    val surname = extractSurname(currentCardholderName) ?: getRandomSurname()
    val firstName = when (option) {
        DigitizationOption.Normal -> getRandomFirstName()
        DigitizationOption.YellowFlow -> "YELLOW"
        DigitizationOption.RedFlow -> "RED"
    }
    return "$firstName $surname"
}

fun generateRandomLastDigits(): String {
    return Random.nextInt(0, 10_000).toString().padStart(4, '0')
}

private fun getRandomFirstName(): String {
    val firstNames = listOf("Mingo", "Agnese", "Ugis", "Lina", "Toms", "Anna")
    return firstNames.random(Random)
}

private fun getRandomSurname(): String {
    val surnames = listOf("Bingo", "Ozols", "Berzins", "Kalnins", "Liepa", "Andersone")
    return surnames.random(Random)
}

private fun extractSurname(cardholderName: String): String? {
    val parts = cardholderName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        parts.drop(1).joinToString(" ")
    } else {
        null
    }
}
