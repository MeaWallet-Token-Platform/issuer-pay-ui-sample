package com.paymentology.dxp.issuerpay.sample.issuerpay.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meawallet.mtp.MeaListener
import com.meawallet.mtp.MeaError
import com.paymentology.dxp.issuerpay.sample.di.appContainer
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.java

class MyFcmListenerService : FirebaseMessagingService() {
    companion object {
        private val TAG = MyFcmListenerService::class.java.simpleName
        private val sLastReceivedToken: MutableStateFlow<String> = MutableStateFlow("")

        fun getLastReceivedToken(): StateFlow<String> {
            return sLastReceivedToken
        }
    }

    private val tokenPlatform: TokenPlatform by lazy {
        appContainer.tokenPlatform
    }

    private lateinit var pushCoordinator: PushMessageCoordinator

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(newToken: String) {
        Log.d(TAG, "onNewToken(): newToken: $newToken")

        sLastReceivedToken.value = newToken

        if (!tokenPlatform.isInitialized()) {
            tokenPlatform.initialize(this)
        }

        if (!tokenPlatform.isRegistered()) {

            return
        }

        tokenPlatform.updateDeviceInfo(newToken, null, object : MeaListener {
            override fun onSuccess() {
                Log.d(TAG, "onNewToken(): tokenPlatform.updateDeviceInfo.onSuccess().")
            }

            override fun onFailure(error: MeaError) {
                Log.e(TAG, "onNewToken(): tokenPlatform.updateDeviceInfo.onFailure(). ${error.code} : ${error.name}",
                    Throwable(message = error.message)
                )
            }
        })
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val messageData = message.data
        Log.d(TAG, "onMessageReceived(): from: ${message.from}")

        // Delegate all message processing to the coordinator
        getPushCoordinator().processMessage(messageData)
    }

    /**
     * Lazy-initialize push message coordinator.
     */
    private fun getPushCoordinator(): PushMessageCoordinator {
        if (!this::pushCoordinator.isInitialized) {
            pushCoordinator = PushMessageCoordinator(applicationContext, tokenPlatform)
        }
        return pushCoordinator
    }
}