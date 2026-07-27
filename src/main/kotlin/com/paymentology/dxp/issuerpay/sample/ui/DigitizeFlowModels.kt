package com.paymentology.dxp.issuerpay.sample.ui

import java.util.Calendar
import kotlin.random.Random

enum class DigitizationOption(val label: String) {
    Normal("Normal"),
    YellowFlow("Yellow Flow"),
    RedFlow("Red Flow")
}

enum class PaymentNetwork(val label: String) {
    Mastercard("Mastercard"),
    Visa("VISA")
}

enum class DigitizationMethod(val label: String) {
    PAN("PAN"),
    CARD_ID("Card Id"),
    ENCRYPTED_PAN("Encrypted PAN")
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
