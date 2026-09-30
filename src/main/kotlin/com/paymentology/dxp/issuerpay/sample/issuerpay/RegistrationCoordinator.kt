package com.paymentology.dxp.issuerpay.sample.issuerpay

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.paymentology.dxp.issuerpay.ui.compose.core.api.IssuerPayListener
import com.paymentology.dxp.issuerpay.ui.compose.core.api.InitializationHelper
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PlatformError
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PushServiceInstanceManager
import com.paymentology.dxp.issuerpay.ui.compose.core.api.RegistrationHelper
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    }

    private val context = appContext.applicationContext
    private val coordinatorScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
        unregisterNetworkCallbackIfRegistered()
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
                    withTimeout(REGISTRATION_TIMEOUT_MS) {
                        awaitRegistration()
                    }
                }

                if (!isStarted) {
                    return@withLock
                }

                if (registrationResult.isSuccess) {
                    _registrationState.value = RegistrationState.Registered
                } else {
                    val failure = registrationResult.exceptionOrNull()
                    _registrationState.value = RegistrationState.Failed(
                        reason = if (failure is TimeoutCancellationException) {
                            RegistrationFailureReason.RegistrationTimedOut
                        } else {
                            RegistrationFailureReason.RegistrationFailed
                        },
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

    private suspend fun awaitRegistration() {
        suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)
            continuation.invokeOnCancellation {
                completed.set(true)
            }

            RegistrationHelper(
                context = context,
                tokenPlatform = tokenPlatform,
                pushServiceInstanceManager = pushServiceInstanceManager,
                initializationHelper = initializationHelper
            ).registerWallet(
                "en",
                object : IssuerPayListener {
                    override fun onSuccess() {
                        if (!completed.compareAndSet(false, true) || !continuation.isActive) return
                        continuation.resume(Unit)
                    }

                    override fun onFailure(error: PlatformError) {
                        if (!completed.compareAndSet(false, true) || !continuation.isActive) return
                        continuation.resumeWithException(RegistrationFailedException(error))
                    }
                }
            )
        }
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

    private class RegistrationFailedException(
        error: PlatformError
    ) : IllegalStateException(error.toString())
}
