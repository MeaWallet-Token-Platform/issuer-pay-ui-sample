package com.paymentology.dxp.issuerpay.sample.issuerpay.messaging

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.meawallet.mtp.MeaCheckedException
import com.meawallet.mtp.MeaTransactionAuthorizationStatus
import com.paymentology.dxp.issuerpay.sample.MainActivity
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform

/**
 * Coordinator for processing Firebase Cloud Messaging (FCM) push messages.
 *
 * Responsibilities:
 * - Parse and validate incoming push messages
 * - Handle transaction notifications (approved, declined, reversed)
 * - Forward non-transaction messages to SDK remote notification service
 * - Show user-facing notifications
 */
class PushMessageCoordinator(
    private val context: Context,
    private val tokenPlatform: TokenPlatform
) {
    companion object {
        private val TAG = PushMessageCoordinator::class.java.simpleName
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var notificationHelper: NotificationHelper

    /**
     * Process an incoming remote message.
     *
     * Returns true if message was processed, false if ignored.
     */
    fun processMessage(messageData: Map<String, String>): Boolean {
        Log.d(TAG, "processMessage(): data: $messageData")

        // Validate this is a Mea (MTP SDK) message
        if (!tokenPlatform.rns.isMeaRemoteMessage(messageData)) {
            return false
        }

        try {
            // Handle transaction messages separately (these show notifications)
            if (tokenPlatform.rns.isMeaTransactionMessage(messageData)) {
                handleTransactionMessage(messageData)
                return true
            }

            // Forward all other Mea messages to SDK's remote notification service
            tokenPlatform.rns.onMessageReceived(messageData)
            Log.d(TAG, "processMessage(): forwarded to SDK RNS successfully")
            return true

        } catch (exception: MeaCheckedException) {
            Log.e(TAG, "Failed to process push message", exception)
            val error = exception.meaError
            showErrorNotification(error.message + " " + error.cardId, "Process PUSH message error " + error.name)
            return false
        }
    }

    /**
     * Handle transaction-specific push messages.
     *
     * Parses transaction details (amount, merchant, status) and shows notification.
     * Ignores CLEARED transactions (already processed).
     */
    private fun handleTransactionMessage(messageData: Map<String, String>) {
        val transactionMessage = tokenPlatform.rns.parseTransactionMessage(messageData)
        Log.d(TAG, "handleTransactionMessage(): $transactionMessage")

        val authorizationStatus = transactionMessage?.authorizationStatus

        // Skip CLEARED transactions (user already saw them)
        if (authorizationStatus == MeaTransactionAuthorizationStatus.CLEARED) {
            Log.d(TAG, "handleTransactionMessage(): ignoring CLEARED transaction")
            return
        }

        val amount = transactionMessage?.amount ?: ""
        val currencyCode = transactionMessage?.currencyCode ?: ""
        val merchantName = transactionMessage?.merchantName ?: ""

        // Build notification text: amount currency (status if declined/reversed)
        val notificationText = buildString {
            append(amount)
            append(" ")
            append(currencyCode)
            if (authorizationStatus == MeaTransactionAuthorizationStatus.DECLINED ||
                authorizationStatus == MeaTransactionAuthorizationStatus.REVERSED
            ) {
                append(" (").append(authorizationStatus).append(")")
            }
        }

        showTransactionNotification(notificationText, merchantName)
    }

    /**
     * Show a transaction notification to the user.
     */
    private fun showTransactionNotification(text: String, title: String) {
        val notification = getNotificationHelper().getNotification(text, title, createMainActivityPendingIntent())
        getNotificationHelper().notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show an error notification.
     */
    private fun showErrorNotification(text: String, title: String) {
        val notification = getNotificationHelper().getNotification(text, title, createMainActivityPendingIntent())
        getNotificationHelper().notify(NOTIFICATION_ID, notification)
    }

    /**
     * Create a pending intent that launches MainActivity.
     */
    private fun createMainActivityPendingIntent(): PendingIntent {
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Lazy-initialize notification helper.
     */
    private fun getNotificationHelper(): NotificationHelper {
        if (!this::notificationHelper.isInitialized) {
            notificationHelper = NotificationHelper(context)
        }
        return notificationHelper
    }
}
