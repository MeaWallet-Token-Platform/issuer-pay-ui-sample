package com.paymentology.dxp.issuerpay.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paymentology.dxp.issuerpay.sample.messaging.PushServiceInstanceManagerImpl
import com.paymentology.dxp.issuerpay.sample.di.appContainer
import com.paymentology.dxp.issuerpay.sample.ui.SampleAppScreen
import com.paymentology.dxp.issuerpay.sample.ui.theme.MyComposeAppTheme
import com.paymentology.dxp.issuerpay.ui.compose.core.api.RegistrationHelper
import kotlin.getValue


/**
 * Main entry point for the Issuer Pay UI sample app.
 *
 * The key client integration demonstrated here is wallet/platform registration via
 * [RegistrationHelper] after app startup.
 */
class MainActivity : ComponentActivity() {
    private val tokenPlatform by lazy { appContainer.tokenPlatform }
    private val initializationHelper by lazy { appContainer.initializationHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyComposeAppTheme {
                SampleAppScreen(
                    tokenPlatform = tokenPlatform
                )
            }
        }

        // Primary client integration: register the wallet/platform for SDK operations.
        val registrationHelper = RegistrationHelper(
            context = application,
            tokenPlatform = tokenPlatform,
            pushServiceInstanceManager = PushServiceInstanceManagerImpl,
            initializationHelper = initializationHelper
        )

        registrationHelper.registerWallet("en", null)
    }
}
