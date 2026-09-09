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
import androidx.compose.ui.unit.dp
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
        SettingRow("SDK Info", state.sdkInfo)
        SettingRow("Initialized", state.initialized)
        SettingRow("Registered", state.registered)
        SettingRow("Msg Token", state.msgToken.ifBlank { "-" })
        SettingRow("Secure NFC supported", state.secureNfcSupported)
        SettingRow("Secure NFC enabled", state.secureNfcEnabled)
        SettingRow("Default Payment App", state.defaultPaymentApp)
        SettingRow("User auth", state.userAuthenticated)

        if (!state.isDefaultPaymentApp) {
            OutlinedButton(
                onClick = {
                    (context as? Activity)?.let { activity ->
                        onSetDefaultPaymentApp(activity)
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Set as default app")
            }
        }

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Refresh")
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
