package com.paymentology.dxp.issuerpay.sample.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onRefresh: () -> Unit,
    onSetDefaultPaymentApp: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SettingRow(stringResource(R.string.ui_sdk_info_label), state.sdkInfo)
        SettingRow(stringResource(R.string.ui_initialized_label), state.initialized)
        SettingRow(stringResource(R.string.ui_registered_label), state.registered)
        SettingRow(stringResource(R.string.ui_msg_token_label), state.msgToken.ifBlank { "-" })
        SettingRow(stringResource(R.string.ui_secure_nfc_supported_label), state.secureNfcSupported)
        SettingRow(stringResource(R.string.ui_secure_nfc_enabled_label), state.secureNfcEnabled)
        SettingRow(stringResource(R.string.ui_default_payment_app_label), state.defaultPaymentApp)
        SettingRow(stringResource(R.string.ui_user_auth_label), state.userAuthenticated)

        if (!state.isDefaultPaymentApp) {
            OutlinedButton(
                onClick = {
                    (context as? Activity)?.let { activity ->
                        onSetDefaultPaymentApp(activity)
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                    Text(stringResource(R.string.ui_set_as_default_app))
            }
        }

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.ui_refresh))
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
