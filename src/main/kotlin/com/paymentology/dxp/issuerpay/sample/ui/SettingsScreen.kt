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
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationFailureReason
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationState
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
        val booleanUnknown = stringResource(R.string.ui_value_unknown)
        SettingRow(stringResource(R.string.ui_sdk_info_label), state.sdkInfo)
        SettingRow(stringResource(R.string.ui_initialized_label), state.initialized.toDisplayText())
        SettingRow(stringResource(R.string.ui_registered_label), state.registrationState.toDisplayText())
        SettingRow(stringResource(R.string.ui_msg_token_label), state.msgToken.ifBlank { booleanUnknown })
        SettingRow(stringResource(R.string.ui_secure_nfc_supported_label), state.secureNfcSupported.toDisplayText())
        SettingRow(stringResource(R.string.ui_secure_nfc_enabled_label), state.secureNfcEnabled.toDisplayText())
        SettingRow(stringResource(R.string.ui_default_payment_app_label), state.isDefaultPaymentApp.toDisplayText())
        SettingRow(stringResource(R.string.ui_user_auth_label), state.userAuthenticated.toDisplayText())

        if (state.isDefaultPaymentApp == false) {
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

        if (state.registrationState is RegistrationState.Failed) {
            Text(
                text = state.registrationState.reason.toDisplayErrorText(),
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

@Composable
private fun Boolean?.toDisplayText(): String = when (this) {
    true -> stringResource(R.string.ui_value_true)
    false -> stringResource(R.string.ui_value_false)
    null -> stringResource(R.string.ui_value_unknown)
}

@Composable
private fun RegistrationState.toDisplayText(): String = when (this) {
    RegistrationState.Registered -> stringResource(R.string.ui_registration_registered)
    RegistrationState.Registering -> stringResource(R.string.ui_registration_registering)
    RegistrationState.Unregistered -> stringResource(R.string.ui_registration_not_registered)
    is RegistrationState.Failed -> stringResource(R.string.ui_registration_not_registered)
}

@Composable
private fun RegistrationFailureReason.toDisplayErrorText(): String = when (this) {
    RegistrationFailureReason.NoNetwork -> stringResource(R.string.ui_registration_error_no_network)
    RegistrationFailureReason.MissingNetworkPermission -> stringResource(R.string.ui_registration_error_missing_network_permission)
    RegistrationFailureReason.NetworkMonitorUnavailable -> stringResource(R.string.ui_registration_error_network_monitor_unavailable)
    RegistrationFailureReason.RegistrationFailed -> stringResource(R.string.ui_registration_error_failed)
    RegistrationFailureReason.RegistrationTimedOut -> stringResource(R.string.ui_registration_error_timed_out)
}
