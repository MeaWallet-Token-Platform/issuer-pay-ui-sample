package com.paymentology.dxp.issuerpay.sample.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paymentology.dxp.issuerpay.sample.R


@Composable
internal fun PanDigitizationSection(
    selectedDigitizationOption: DigitizationOption,
    onSelectedDigitizationOption: (DigitizationOption) -> Unit,
    selectedNetwork: PaymentNetwork,
    onSelectedNetwork: (PaymentNetwork) -> Unit,
    pan: String,
    onPanChange: (String) -> Unit,
    onPanGenerate: () -> Unit,
    cardholderName: String,
    onCardholderNameChange: (String) -> Unit,
    onCardholderNameGenerate: () -> Unit,
    monthOptions: List<Int>,
    selectedMonth: Int,
    onSelectedMonth: (Int) -> Unit,
    yearOptions: List<Int>,
    selectedYear: Int,
    onSelectedYear: (Int) -> Unit
) {
    val digitizationOptionLabels = mapOf(
        DigitizationOption.Normal to stringResource(R.string.ui_digitization_option_normal),
        DigitizationOption.YellowFlow to stringResource(R.string.ui_digitization_option_yellow_flow),
        DigitizationOption.RedFlow to stringResource(R.string.ui_digitization_option_red_flow)
    )
    val paymentNetworkLabels = mapOf(
        PaymentNetwork.Mastercard to stringResource(R.string.ui_payment_network_mastercard),
        PaymentNetwork.Visa to stringResource(R.string.ui_payment_network_visa)
    )

    M3ExposedDropdown(
        label = stringResource(R.string.ui_choose_digitization_type),
        options = DigitizationOption.entries,
        selected = selectedDigitizationOption,
        onSelected = onSelectedDigitizationOption,
        optionLabel = { digitizationOptionLabels[it].orEmpty() },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    M3ExposedDropdown(
        label = stringResource(R.string.ui_choose_payment_network),
        options = PaymentNetwork.entries,
        selected = selectedNetwork,
        onSelected = onSelectedNetwork,
        optionLabel = { paymentNetworkLabels[it].orEmpty() },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    LabeledInputWithGetButton(
        label = stringResource(R.string.ui_card_pan),
        value = pan,
        onValueChange = onPanChange,
        onGetClick = onPanGenerate
    )

    Spacer(modifier = Modifier.height(8.dp))

    LabeledInputWithGetButton(
        label = stringResource(R.string.ui_cardholder_name),
        value = cardholderName,
        onValueChange = onCardholderNameChange,
        onGetClick = onCardholderNameGenerate
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        M3ExposedDropdown(
            label = stringResource(R.string.ui_valid_thru_month),
            options = monthOptions,
            selected = selectedMonth,
            onSelected = onSelectedMonth,
            optionLabel = { it.toString() },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        M3ExposedDropdown(
            label = stringResource(R.string.ui_valid_thru_year),
            options = yearOptions,
            selected = selectedYear,
            onSelected = onSelectedYear,
            optionLabel = { it.toString() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun CardIdDigitizationSection(
    cardId: String,
    onCardIdChange: (String) -> Unit,
    onCardIdGenerate: () -> Unit,
    cardSecret: String,
    onCardSecretChange: (String) -> Unit,
    onCardSecretGenerate: () -> Unit,
    cardBin: String,
    onCardBinChange: (String) -> Unit,
    onCardBinGenerate: () -> Unit
) {
    LabeledInputWithGetButton(
        label = stringResource(R.string.ui_card_id),
        value = cardId,
        onValueChange = onCardIdChange,
        onGetClick = onCardIdGenerate
    )

    Spacer(modifier = Modifier.height(8.dp))

    LabeledInputWithGetButton(
        label = stringResource(R.string.ui_card_secret),
        value = cardSecret,
        onValueChange = onCardSecretChange,
        onGetClick = onCardSecretGenerate
    )

    Spacer(modifier = Modifier.height(8.dp))

    LabeledInputWithGetButton(
        label = stringResource(R.string.ui_bin),
        value = cardBin,
        onValueChange = onCardBinChange,
        onGetClick = onCardBinGenerate
    )
}

@Composable
internal fun EncryptedPanDigitizationSection(
    encryptedCardData: String,
    onEncryptedCardDataChange: (String) -> Unit,
    publicKeyFingerprint: String,
    onPublicKeyFingerprintChange: (String) -> Unit,
    encryptedKey: String,
    onEncryptedKeyChange: (String) -> Unit,
    initialVector: String,
    onInitialVectorChange: (String) -> Unit,
    onGetStaticData: () -> Unit,
    onLoadFromFile: () -> Unit
) {
    OutlinedTextField(
        value = encryptedCardData,
        onValueChange = onEncryptedCardDataChange,
        label = { Text(stringResource(R.string.ui_encrypted_card_data)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = publicKeyFingerprint,
        onValueChange = onPublicKeyFingerprintChange,
        label = { Text(stringResource(R.string.ui_public_key_fingerprint)) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = encryptedKey,
        onValueChange = onEncryptedKeyChange,
        label = { Text(stringResource(R.string.ui_encrypted_key)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = initialVector,
        onValueChange = onInitialVectorChange,
        label = { Text(stringResource(R.string.ui_initial_vector)) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onGetStaticData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.ui_get_static_data))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLoadFromFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.ui_load_from_file))
        }
    }
}

@Composable
private fun LabeledInputWithGetButton(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onGetClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = onGetClick, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.ui_get))
        }
    }
}
