package com.paymentology.dxp.issuerpay.sample.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.sample.messaging.PushServiceInstanceManagerImpl
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationCoordinator
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationState
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PushServiceInstanceManager
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val sdkInfo: String = "-",
    val initialized: String = "-",
    val registered: String = "-",
    val msgToken: String = "-",
    val secureNfcSupported: String = "-",
    val secureNfcEnabled: String = "-",
    val defaultPaymentApp: String = "-",
    val userAuthenticated: String = "-",
    val isDefaultPaymentApp: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

sealed interface SettingsIntent {
    data object Refresh : SettingsIntent
    data class SetDefaultPaymentApp(val activity: Activity) : SettingsIntent
}

class SettingsViewModel(
    appContext: Context,
    private val tokenPlatform: TokenPlatform,
    private val registrationCoordinator: RegistrationCoordinator,
    private val pushServiceInstanceManager: PushServiceInstanceManager
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val applicationContext = appContext.applicationContext

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeRegistrationState()
        observePushToken()
        dispatch(SettingsIntent.Refresh)
    }

    fun dispatch(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Refresh -> refresh()
            is SettingsIntent.SetDefaultPaymentApp -> setDefaultPaymentApplication(intent.activity)
        }
    }

    private fun observeRegistrationState() {
        viewModelScope.launch {
            registrationCoordinator.registrationState.collectLatest { registrationState ->
                _state.update { current ->
                    current.copy(registered = registrationState.toRegisteredDisplayText())
                }

                if (registrationState == RegistrationState.Registered) {
                    dispatch(SettingsIntent.Refresh)
                }
            }
        }
    }

    private fun observePushToken() {
        viewModelScope.launch {
            pushServiceInstanceManager.getObservableIdToken(viewModelScope).collectLatest { token ->
                _state.update { it.copy(msgToken = token.ifBlank { "-" }) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    SettingsSnapshot(
                        sdkInfo = listOf(
                            tokenPlatform.configuration.versionName(),
                            tokenPlatform.configuration.buildType(),
                            tokenPlatform.configuration.cdCvmModel()
                        ).joinToString("; "),
                        initialized = tokenPlatform.isInitialized().toString(),
                        secureNfcSupported = tokenPlatform.isSecureNfcSupported().toString(),
                        secureNfcEnabled = tokenPlatform.isSecureNfcEnabled().toString(),
                        defaultPaymentApp = tokenPlatform.isDefaultPaymentApplication(applicationContext).toString(),
                        userAuthenticated = if (tokenPlatform.isInitialized()) {
                            tokenPlatform.cdCvm.isCardholderAuthenticated().toString()
                        } else {
                            "false"
                        }
                    )
                }
            }

            result.fold(
                onSuccess = { snapshot ->
                    _state.update {
                        it.copy(
                            sdkInfo = snapshot.sdkInfo,
                            initialized = snapshot.initialized,
                            secureNfcSupported = snapshot.secureNfcSupported,
                            secureNfcEnabled = snapshot.secureNfcEnabled,
                            defaultPaymentApp = snapshot.defaultPaymentApp,
                            userAuthenticated = snapshot.userAuthenticated,
                            isDefaultPaymentApp = snapshot.defaultPaymentApp.equals("true", ignoreCase = true),
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    Log.e(TAG, "Failed to refresh settings.", throwable)
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = throwable.message ?: applicationContext.getString(R.string.ui_failed_to_load_settings)
                        )
                    }
                }
            )
        }
    }

    private fun setDefaultPaymentApplication(activity: Activity) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    tokenPlatform.setDefaultPaymentApplication(activity, 420)
                }
            }

            result.fold(
                onSuccess = {
                    dispatch(SettingsIntent.Refresh)
                },
                onFailure = { throwable ->
                    Log.e(TAG, "Failed to set default payment application.", throwable)
                    _state.update {
                        it.copy(
                            errorMessage = throwable.message ?: applicationContext.getString(R.string.ui_failed_to_set_default_payment_application)
                        )
                    }
                }
            )
        }
    }

    private data class SettingsSnapshot(
        val sdkInfo: String,
        val initialized: String,
        val secureNfcSupported: String,
        val secureNfcEnabled: String,
        val defaultPaymentApp: String,
        val userAuthenticated: String
    )

    private fun RegistrationState.toRegisteredDisplayText(): String = when (this) {
        RegistrationState.Registered -> "true"
        RegistrationState.Registering -> "registering"
        RegistrationState.Unregistered -> "false"
        is RegistrationState.Failed -> "false"
    }
}

class SettingsViewModelFactory(
    private val appContext: Context,
    private val tokenPlatform: TokenPlatform,
    private val registrationCoordinator: RegistrationCoordinator,
    private val pushServiceInstanceManager: PushServiceInstanceManager = PushServiceInstanceManagerImpl
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    appContext = appContext,
                    tokenPlatform = tokenPlatform,
                    registrationCoordinator = registrationCoordinator,
                    pushServiceInstanceManager = pushServiceInstanceManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
