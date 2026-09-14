package com.paymentology.dxp.issuerpay.sample.issuerpay

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.paymentology.dxp.issuerpay.ui.compose.core.api.InitializationHelper
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PushServiceInstanceManager
import com.paymentology.dxp.issuerpay.ui.compose.core.api.RegistrationHelper
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

sealed interface RegistrationState {
    data object Unregistered : RegistrationState
    data object Registering : RegistrationState
    data object Registered : RegistrationState
    data class Failed(
        val reason: RegistrationFailureReason,
        val details: String? = null
    ) : RegistrationState
}

enum class RegistrationFailureReason {
    NoNetwork,
    MissingNetworkPermission,
    NetworkMonitorUnavailable,
    RegistrationFailed,
    RegistrationTimedOut
}

class RegistrationCoordinator(
    appContext: Context,
    private val tokenPlatform: TokenPlatform,
    private val initializationHelper: InitializationHelper,
    private val pushServiceInstanceManager: PushServiceInstanceManager
) {
    companion object {
        private const val REGISTRATION_TIMEOUT_MS = 20_000L
        private const val REGISTRATION_STATE_WAIT_MS = 5_000L
        private const val REGISTRATION_STATE_POLL_INTERVAL_MS = 250L
    }

    private val context = appContext.applicationContext
    private var coordinatorJob: Job = SupervisorJob()
    private var coordinatorScope: CoroutineScope = CoroutineScope(coordinatorJob + Dispatchers.IO)
    private val registrationMutex = Mutex()
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Unregistered)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    private var isStarted = false
    private var isNetworkCallbackRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            ensureRegistered()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                ensureRegistered()
            }
        }

        override fun onLost(network: Network) {
            if (!isNetworkAvailable() && !isCurrentlyRegistered()) {
                _registrationState.value = RegistrationState.Failed(
                    reason = RegistrationFailureReason.NoNetwork
                )
            }
        }
    }

    @Synchronized
    fun start() {
        if (isStarted) {
            ensureRegistered()
            return
        }

        if (!coordinatorJob.isActive) {
            coordinatorJob = SupervisorJob()
            coordinatorScope = CoroutineScope(coordinatorJob + Dispatchers.IO)
        }

        isStarted = true
        registerNetworkCallbackIfPossible()
        ensureRegistered()
    }

    @Synchronized
    fun stop() {
        if (!isStarted) {
            return
        }

        isStarted = false
        unregisterNetworkCallbackIfRegistered()
        coordinatorScope.cancel()
    }

    fun onAppResumed() {
        ensureRegistered()
    }

    fun ensureRegistered() {
        if (!isStarted) {
            return
        }

        coordinatorScope.launch {
            registrationMutex.withLock {
                if (isCurrentlyRegistered()) {
                    _registrationState.value = RegistrationState.Registered
                    return@withLock
                }

                if (!isNetworkAvailable()) {
                    _registrationState.value = RegistrationState.Failed(
                        reason = RegistrationFailureReason.NoNetwork
                    )
                    return@withLock
                }

                _registrationState.value = RegistrationState.Registering

                val registrationResult = runCatching {
                    withTimeout(REGISTRATION_TIMEOUT_MS.milliseconds) {
                        RegistrationHelper(
                            context = context,
                            tokenPlatform = tokenPlatform,
                            pushServiceInstanceManager = pushServiceInstanceManager,
                            initializationHelper = initializationHelper
                        ).registerWallet("en", null)
                    }
                }

                if (waitUntilRegistered()) {
                    _registrationState.value = RegistrationState.Registered
                } else {
                    val failure = registrationResult.exceptionOrNull()
                    val reason = if (failure is TimeoutCancellationException) {
                        RegistrationFailureReason.RegistrationTimedOut
                    } else {
                        RegistrationFailureReason.RegistrationFailed
                    }

                    _registrationState.value = RegistrationState.Failed(
                        reason = reason,
                        details = failure?.message
                    )
                }
            }
        }
    }

    private fun isCurrentlyRegistered(): Boolean {
        return runCatching { tokenPlatform.isRegistered() }.getOrDefault(false)
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun waitUntilRegistered(): Boolean {
        val deadline = System.currentTimeMillis() + REGISTRATION_STATE_WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            if (isCurrentlyRegistered()) {
                return true
            }
            delay(REGISTRATION_STATE_POLL_INTERVAL_MS.milliseconds)
        }
        return isCurrentlyRegistered()
    }

    @Synchronized
    private fun registerNetworkCallbackIfPossible() {
        if (isNetworkCallbackRegistered) {
            return
        }

        val manager = connectivityManager ?: return
        try {
            manager.registerDefaultNetworkCallback(networkCallback)
            isNetworkCallbackRegistered = true
        } catch (_: SecurityException) {
            _registrationState.value = RegistrationState.Failed(
                reason = RegistrationFailureReason.MissingNetworkPermission
            )
        } catch (_: IllegalArgumentException) {
            _registrationState.value = RegistrationState.Failed(
                reason = RegistrationFailureReason.NetworkMonitorUnavailable
            )
        }
    }

    @Synchronized
    private fun unregisterNetworkCallbackIfRegistered() {
        if (!isNetworkCallbackRegistered) {
            return
        }

        val manager = connectivityManager ?: return
        try {
            manager.unregisterNetworkCallback(networkCallback)
        } catch (_: IllegalArgumentException) {
            // Callback was already unregistered.
        } finally {
            isNetworkCallbackRegistered = false
        }
    }
}
