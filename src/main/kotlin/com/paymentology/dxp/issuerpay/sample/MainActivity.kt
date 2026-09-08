package com.paymentology.dxp.issuerpay.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paymentology.dxp.issuerpay.sample.di.appContainer
import com.paymentology.dxp.issuerpay.sample.ui.SampleAppScreen
import com.paymentology.dxp.issuerpay.sample.ui.theme.MyComposeAppTheme
import kotlin.getValue


/**
 * Main entry point for the Issuer Pay UI sample app.
 *
 * Triggers app-scoped registration retry checks when the activity is created/resumed.
 */
class MainActivity : ComponentActivity() {
    private val tokenPlatform by lazy { appContainer.tokenPlatform }
    private val registrationCoordinator by lazy { appContainer.registrationCoordinator }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyComposeAppTheme {
                SampleAppScreen(
                    tokenPlatform = tokenPlatform,
                    registrationCoordinator = registrationCoordinator
                )
            }
        }
        registrationCoordinator.start()
    }

    override fun onResume() {
        super.onResume()
        registrationCoordinator.onAppResumed()
    }
}
