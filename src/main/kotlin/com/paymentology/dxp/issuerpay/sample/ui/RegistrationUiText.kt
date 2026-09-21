package com.paymentology.dxp.issuerpay.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationFailureReason
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationState

@Composable
fun RegistrationState.toDisplayLabel(): String = when (this) {
    RegistrationState.Registered -> stringResource(R.string.ui_registration_registered)
    RegistrationState.Registering -> stringResource(R.string.ui_registration_registering)
    RegistrationState.Unregistered -> stringResource(R.string.ui_registration_not_registered)
    is RegistrationState.Failed -> stringResource(R.string.ui_registration_not_registered)
}

@Composable
fun RegistrationFailureReason.toDisplayErrorText(): String = when (this) {
    RegistrationFailureReason.NoNetwork -> stringResource(R.string.ui_registration_error_no_network)
    RegistrationFailureReason.MissingNetworkPermission -> stringResource(R.string.ui_registration_error_missing_network_permission)
    RegistrationFailureReason.NetworkMonitorUnavailable -> stringResource(R.string.ui_registration_error_network_monitor_unavailable)
    RegistrationFailureReason.RegistrationFailed -> stringResource(R.string.ui_registration_error_failed)
}
