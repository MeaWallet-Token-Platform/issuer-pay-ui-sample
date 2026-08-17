package com.paymentology.dxp.issuerpay.sample.messaging

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meawallet.mtp.*
import com.paymentology.dxp.issuerpay.sample.MainActivity
import com.paymentology.dxp.issuerpay.sample.di.appContainer
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.isInitialized
import kotlin.jvm.java

class MyFcmListenerService : FirebaseMessagingService() {
    companion object {
        private val TAG = MyFcmListenerService::class.java.simpleName
        private const val NOTIFICATION_ID = 1

        private val sLastReceivedToken: MutableStateFlow<String> = MutableStateFlow("")

        fun getLastReceivedToken(): StateFlow<String> {
            return sLastReceivedToken
        }
    }

    private lateinit var sNotificationHelper: NotificationHelper

    private val tokenPlatform: TokenPlatform by lazy {
        appContainer.tokenPlatform
    }

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

        Log.d(TAG, "onMessageReceived(): from: ${message.from}, data: $messageData")

        if (!tokenPlatform.rns.isMeaRemoteMessage(messageData)) {
            return
        }

        try {
            if (tokenPlatform.rns.isMeaTransactionMessage(messageData)) {
                val transactionMessage = tokenPlatform.rns.parseTransactionMessage(messageData)
                Log.d(TAG, "Transaction PUSH Message: $transactionMessage")

                val authorizationStatus = transactionMessage?.authorizationStatus

                // Ignore if transaction status is CLEARED
                if (authorizationStatus == MeaTransactionAuthorizationStatus.CLEARED) {
                    return
                }

                val amount = transactionMessage?.amount
                val currencyCode =
                    if (transactionMessage != null) transactionMessage.currencyCode else ""

                val merchantName =
                    if (transactionMessage != null) transactionMessage.merchantName else ""

                val notificationText = kotlin.text.StringBuilder()
                    .append(amount)
                    .append(" ")
                    .append(currencyCode)
                if (authorizationStatus == MeaTransactionAuthorizationStatus.DECLINED
                    || authorizationStatus == MeaTransactionAuthorizationStatus.REVERSED
                ) {
                    notificationText.append(" (").append(authorizationStatus.toString()).append(")")
                }

                showTransactionNotification(
                    applicationContext,
                    notificationText.toString(),
                    merchantName
                )

            } else {
                // Forward received remote message
                tokenPlatform.rns.onMessageReceived(messageData)
                Log.d(TAG, "onMessageReceived success.")
            }
        } catch (exception: MeaCheckedException) {
            Log.e(TAG, "Failed to process PUSH message.", exception)
            val error = exception.meaError

            showTransactionNotification(
                applicationContext,
                error.message + " " + error.cardId, "Process PUSH message error " + error.name
            )

        }
    }

    private fun showTransactionNotification(context: Context, text: String, title: String) {
        val mainActivityIntent = Intent(
            context,
            MainActivity::class.java
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification: NotificationCompat.Builder =
            getNotificationHelper(context).getNotification(text, title, pendingIntent)
        getNotificationHelper(context).notify(NOTIFICATION_ID, notification)
    }

    private fun getNotificationHelper(context: Context): NotificationHelper {
        if (!this::sNotificationHelper.isInitialized) {
            sNotificationHelper = NotificationHelper(context)
        }
        return sNotificationHelper
    }
}