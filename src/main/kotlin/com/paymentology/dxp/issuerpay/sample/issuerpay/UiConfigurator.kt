package com.paymentology.dxp.issuerpay.sample.issuerpay

import androidx.compose.material3.Text
import com.paymentology.dxp.issuerpay.ui.compose.uiconfig.UiComposeConfigHolderImpl

/**
 * Configures optional custom Compose content for Issuer Pay UI screens.
 *
 * This function assigns lambdas on [UiComposeConfigHolderImpl] that are rendered by the library
 * during payment and digitization flows. Call it during app initialization.
 *
 * If custom behavior is not required, keep the default implementation by not overriding these
 * properties.
 */
fun configureCustomUi() {
    /**
     * Content for the payment "Ready to Pay" screen.
     *
     * Property type: `@Composable () -> Unit`.
     * Displayed after the user initiates payment from the app.
     */
    UiComposeConfigHolderImpl.readyForPaymentContent = {
        Text("Try tapping!")
    }

    /**
     * Content for the payment "Submitted" screen.
     *
     * Property type: `@Composable (MeaContactlessTransactionData?) -> Unit`.
     * Displayed when terminal communication has completed and submission succeeded. The payment
     * can still be declined later by the acquirer or issuer.
     *
     * @param data [com.meawallet.mtp.MeaContactlessTransactionData] payload from SDK; nullable
     * when transaction details are unavailable.
     */
    UiComposeConfigHolderImpl.transactionSubmittedContent = { data ->
        Text(
            "Crazy amount submitted: " +
                (data?.amount ?: "but I don't know")
        )
    }

    /**
     * Content for the payment "Failed" screen.
     *
     * Property type: `@Composable () -> Unit`.
     * Displayed when submission fails during terminal communication.
     */
    UiComposeConfigHolderImpl.transactionFailedContent = {
        Text("Ohhh nooooo, it failed!")
    }

    /**
     * Content for the digitization welcome screen.
     *
     * Property type: `@Composable () -> Unit`.
     * Displayed as the first screen when card digitization starts.
     */
    UiComposeConfigHolderImpl.digitizationWelcomeContent = {
        Text("Welcome to digitization")
    }

    /**
     * Content for the digitization success screen.
     *
     * Property type: `@Composable (String?) -> Unit`.
     * Displayed when digitization completes and the card reaches the Digitized state. The app may
     * still need to wait for push provisioning to activate payment credentials.
     *
     * @param lastDigits Last four digits of the card as `String?`; nullable when not provided.
     */
    UiComposeConfigHolderImpl.digitizationCompletedContent = { lastDigits ->
        Text("Digitization completed for card: ${lastDigits ?: "unknown"}")
    }

    /**
     * Content for the digitization failure screen.
     *
     * Property type: `@Composable (String?) -> Unit`.
     * Displayed when digitization fails.
     *
     * @param errorMessage Failure reason as `String?`; nullable when SDK does not provide details.
     */
    UiComposeConfigHolderImpl.digitizationFailedContent = { errorMessage ->
        Text("Digitization failed: ${errorMessage ?: "unknown error"}")
    }
}
