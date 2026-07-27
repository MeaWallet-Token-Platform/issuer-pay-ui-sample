package com.paymentology.dxp.issuerpay.sample.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paymentology.dxp.issuerpay.sample.messaging.PushServiceInstanceManagerImpl
import com.paymentology.dxp.issuerpay.ui.compose.platform.TokenPlatform

@Composable
fun SettingsScreen(
    tokenPlatform: TokenPlatform,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val msgToken by PushServiceInstanceManagerImpl
        .getObservableIdToken(coroutineScope)
        .collectAsState(initial = "")
    var refreshTick by remember { mutableStateOf(0) }

    val sdkInfo = remember(refreshTick) {
        runCatching {
            listOf(
                tokenPlatform.configuration.versionName(),
                tokenPlatform.configuration.buildType(),
                tokenPlatform.configuration.cdCvmModel()
            ).joinToString("; ")
        }.getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }

    val initialized = remember(refreshTick) {
        runCatching { tokenPlatform.isInitialized().toString() }
            .getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }
    val registered = remember(refreshTick) {
        runCatching { tokenPlatform.isRegistered().toString() }
            .getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }
    val secureNfcSupported = remember(refreshTick) {
        runCatching { tokenPlatform.isSecureNfcSupported().toString() }
            .getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }
    val secureNfcEnabled = remember(refreshTick) {
        runCatching { tokenPlatform.isSecureNfcEnabled().toString() }
            .getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }
    val defaultPaymentApp = remember(refreshTick) {
        runCatching { tokenPlatform.isDefaultPaymentApplication(context).toString() }
            .getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }
    val userAuthenticated = remember(refreshTick) {
        runCatching {
            if (tokenPlatform.isInitialized()) {
                tokenPlatform.cdCvm.isCardholderAuthenticated().toString()
            } else {
                "false"
            }
        }.getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
    }

    val isDefaultPaymentApp = defaultPaymentApp.equals("true", ignoreCase = true)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SettingRow("SDK Info", sdkInfo)
        SettingRow("Initialized", initialized)
        SettingRow("Registered", registered)
        SettingRow("Msg Token", msgToken.ifBlank { "-" })
        SettingRow("Secure NFC supported", secureNfcSupported)
        SettingRow("Secure NFC enabled", secureNfcEnabled)
        SettingRow("Default Payment App", defaultPaymentApp)
        SettingRow("User auth", userAuthenticated)

        if (!isDefaultPaymentApp && context is ComponentActivity) {
            OutlinedButton(
                onClick = { tokenPlatform.setDefaultPaymentApplication(context, 420) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Set as default app")
            }
        }

        OutlinedButton(
            onClick = { refreshTick++ },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Refresh")
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
