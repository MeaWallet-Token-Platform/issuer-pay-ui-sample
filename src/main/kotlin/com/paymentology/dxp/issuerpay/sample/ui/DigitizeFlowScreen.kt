package com.paymentology.dxp.issuerpay.sample.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationCoordinator
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationState
import com.paymentology.dxp.issuerpay.sample.utils.EncryptedDataReader
import com.paymentology.dxp.issuerpay.sample.utils.RandomPanBuilder
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PlatformError
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.BackgroundBrush
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.CardDigitizationButton
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.CardDigitizationCallback
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.DigitizationLauncherInput
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.DigitizationParameters
import com.paymentology.dxp.issuerpay.ui.compose.digitization.api.PaymentCardConfig


@Composable
fun DigitizeFlowScreen(
    registrationCoordinator: RegistrationCoordinator,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cardDigitizationStatus by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val registrationState by registrationCoordinator.registrationState.collectAsState()
    val registrationErrorMessage = (registrationState as? RegistrationState.Failed)?.message.orEmpty()

    var selectedDigitizationMethod by remember { mutableStateOf(DigitizationMethod.PAN) }
    var selectedDigitizationOption by remember { mutableStateOf(DigitizationOption.Normal) }
    var selectedNetwork by remember { mutableStateOf(PaymentNetwork.Mastercard) }

    var pan by remember { mutableStateOf("") }
    val currentYear = getCurrentYear()
    val monthOptions = remember { (1..12).toList() }
    val yearOptions = remember(currentYear) { (currentYear..currentYear + 5).toList() }
    var expiryMonth by remember { mutableStateOf(getCurrentMonth()) }
    var expiryYear by remember { mutableStateOf(currentYear + 2) }
    var cardholderName by remember { mutableStateOf("") }
    var isGeneratedCardholderName by remember { mutableStateOf(false) }

    var cardId by remember { mutableStateOf("") }
    var cardSecret by remember { mutableStateOf("") }
    var cardBin by remember { mutableStateOf("") }

    var encryptedCardData by remember { mutableStateOf("") }
    var publicKeyFingerprint by remember { mutableStateOf("") }
    var encryptedKey by remember { mutableStateOf("") }
    var initialVector by remember { mutableStateOf("") }

    val encryptedFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val encryptedData = uri?.let { EncryptedDataReader.readEncryptedDataFromContent(context, it) }
        if (encryptedData != null) {
            encryptedCardData = encryptedData.encryptedCardData
            publicKeyFingerprint = encryptedData.publicKeyFingerprint
            encryptedKey = encryptedData.encryptedKey
            initialVector = encryptedData.iv
        } else {
            errorMessage = "Encrypted data loading failed"
        }
    }

    LaunchedEffect(selectedDigitizationOption) {
        if (selectedDigitizationMethod == DigitizationMethod.PAN && isGeneratedCardholderName) {
            cardholderName = getGeneratedCardholderName(
                option = selectedDigitizationOption,
                currentCardholderName = cardholderName
            )
        }
    }

    LaunchedEffect(Unit) {
        registrationCoordinator.ensureRegistered()
    }

    // Primary client integration #1:
    // Build library DigitizationParameters from UI input before invoking the flow.
    val params = when (selectedDigitizationMethod) {
        DigitizationMethod.PAN -> DigitizationParameters.Pan(
            pan = pan,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cardholderName = cardholderName
        )
        DigitizationMethod.CARD_ID -> DigitizationParameters.Secret(
            cardId = cardId,
            cardSecret = cardSecret,
            bin = cardBin.ifBlank { null }
        )
        DigitizationMethod.ENCRYPTED_PAN -> DigitizationParameters.EncryptedPan(
            encryptedCardData = encryptedCardData,
            publicKeyFingerprint = publicKeyFingerprint,
            encryptedKey = encryptedKey,
            initialVector = initialVector
        )
    }

    val generatedLastDigits = remember(selectedDigitizationMethod) { generateRandomLastDigits() }
    val lastDigits = when (params) {
        is DigitizationParameters.Pan -> params.pan.takeLast(4)
        is DigitizationParameters.Secret -> generatedLastDigits
        is DigitizationParameters.EncryptedPan -> generatedLastDigits
        else -> generatedLastDigits
    }

    // Primary client integration #2:
    // Optional UI customization for the library-provided digitization flow.
    val paymentCardConfig = PaymentCardConfig(
        lastDigits = lastDigits,
        lastDigitsColor = Color.White.toArgb(),
        backgroundColor = MaterialTheme.colorScheme.primary.toArgb(),
        backgroundBrush = BackgroundBrush.Linear(
            colors = listOf(
                Color(0xFF1E3A8A).toArgb(),
                Color(0xFF3B82F6).toArgb()
            )
        ),
//            backgroundResourceId = R.drawable.bg_randombank_card,
//            backgroundAssetPath = "bg_sample_card.png",
//            backgroundImageUri =
//                bitmapInMemory?.let {
//                    saveBitmapToCacheAndGetUri(context, it)
//                }
    )

    val launcherInput = DigitizationLauncherInput(
        params = params,
        paymentCardConfig = paymentCardConfig
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Digitization Flow",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        M3ExposedDropdown(
            label = "Choose digitization method",
            options = DigitizationMethod.entries,
            selected = selectedDigitizationMethod,
            onSelected = { selectedDigitizationMethod = it },
            optionLabel = { it.label },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedDigitizationMethod) {
            DigitizationMethod.PAN -> PanDigitizationSection(
                selectedDigitizationOption = selectedDigitizationOption,
                onSelectedDigitizationOption = { selectedDigitizationOption = it },
                selectedNetwork = selectedNetwork,
                onSelectedNetwork = {
                    selectedNetwork = it
                    pan = ""
                },
                pan = pan,
                onPanChange = { pan = it },
                onPanGenerate = { pan = RandomPanBuilder.getRandomPanForNetwork(selectedNetwork) },
                cardholderName = cardholderName,
                onCardholderNameChange = {
                    cardholderName = it
                    isGeneratedCardholderName = false
                },
                onCardholderNameGenerate = {
                    cardholderName = getGeneratedCardholderName(
                        option = selectedDigitizationOption,
                        currentCardholderName = cardholderName
                    )
                    isGeneratedCardholderName = true
                },
                monthOptions = monthOptions,
                selectedMonth = expiryMonth,
                onSelectedMonth = { expiryMonth = it },
                yearOptions = yearOptions,
                selectedYear = expiryYear,
                onSelectedYear = { expiryYear = it }
            )

            DigitizationMethod.CARD_ID -> CardIdDigitizationSection(
                cardId = cardId,
                onCardIdChange = { cardId = it },
                onCardIdGenerate = { cardId = "20267120" },
                cardSecret = cardSecret,
                onCardSecretChange = { cardSecret = it },
                onCardSecretGenerate = { cardSecret = "LO25RZ53" },
                cardBin = cardBin,
                onCardBinChange = { cardBin = it },
                onCardBinGenerate = { cardBin = "400000" }
            )

            DigitizationMethod.ENCRYPTED_PAN -> EncryptedPanDigitizationSection(
                encryptedCardData = encryptedCardData,
                onEncryptedCardDataChange = { encryptedCardData = it },
                publicKeyFingerprint = publicKeyFingerprint,
                onPublicKeyFingerprintChange = { publicKeyFingerprint = it },
                encryptedKey = encryptedKey,
                onEncryptedKeyChange = { encryptedKey = it },
                initialVector = initialVector,
                onInitialVectorChange = { initialVector = it },
                onGetStaticData = {
                    val encryptedData = EncryptedDataReader.readEncryptedDataFromAssets(context)
                    if (encryptedData != null) {
                        encryptedCardData = encryptedData.encryptedCardData
                        publicKeyFingerprint = encryptedData.publicKeyFingerprint
                        encryptedKey = encryptedData.encryptedKey
                        initialVector = encryptedData.iv
                    } else {
                        errorMessage = "Encrypted data loading failed"
                    }
                },
                onLoadFromFile = { encryptedFilePicker.launch("application/json") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Registration: ${registrationState.toDisplayLabel()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Primary client integration #3:
        // Invoke the library flow entrypoint via CardDigitizationButton.
        CardDigitizationButton(
            label = "Digitize Card",
            enabled = registrationState == RegistrationState.Registered,
            modifier = Modifier.fillMaxWidth(),
            params = launcherInput,
            callback = object : CardDigitizationCallback {
                override fun onCardDigitized(cardId: String) {
                    cardDigitizationStatus = "✓ Card digitized successfully. Card ID: $cardId"
                    errorMessage = ""
                }

                override fun onCardDigitizationFailed(error: PlatformError) {
                    cardDigitizationStatus = "✗ Digitization failed"
                    errorMessage = when (error) {
                        is PlatformError.MtpSdkError -> "${error.code}: ${error.message ?: "Unknown error"}"
                        else -> error.javaClass.name
                    }
                    registrationCoordinator.ensureRegistered()
                }

                override fun onUserCancelled() {
                    cardDigitizationStatus = "User cancelled"
                    errorMessage = ""
                }
            }
        )

        if (cardDigitizationStatus.isNotEmpty()) {
            Text(
                text = cardDigitizationStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val visibleErrorMessage = registrationErrorMessage.ifBlank { errorMessage }

        if (visibleErrorMessage.isNotEmpty()) {
            Text(
                text = "Error: $visibleErrorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

}

private fun RegistrationState.toDisplayLabel(): String = when (this) {
    RegistrationState.Registered -> "Registered"
    RegistrationState.Registering -> "Registering"
    RegistrationState.Unregistered -> "Not registered"
    is RegistrationState.Failed -> "Not registered"
}
