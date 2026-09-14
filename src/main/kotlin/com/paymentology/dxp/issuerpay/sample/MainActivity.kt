package com.paymentology.dxp.issuerpay.sample

import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.paymentology.dxp.issuerpay.sample.di.appContainer
import com.paymentology.dxp.issuerpay.sample.issuerpay.messaging.PushServiceInstanceManagerImpl
import com.paymentology.dxp.issuerpay.sample.ui.SampleAppScreen
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardListViewModel
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.SettingsViewModel
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.cardListViewModelFactory
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.settingsViewModelFactory
import com.paymentology.dxp.issuerpay.sample.ui.theme.MyComposeAppTheme


/**
 * Main entry point for the Issuer Pay UI sample app.
 *
 * Triggers app-scoped registration retry checks when the activity is created/resumed.
 */
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val tokenPlatform by lazy { appContainer.tokenPlatform }
    private val registrationCoordinator by lazy { appContainer.registrationCoordinator }
    private var showResetDialog by mutableStateOf(false)

    private val cardListViewModel: CardListViewModel by viewModels {
        cardListViewModelFactory(
            appContext = applicationContext,
            tokenPlatform = tokenPlatform,
            registrationCoordinator = registrationCoordinator
        )
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        settingsViewModelFactory(
            appContext = applicationContext,
            tokenPlatform = tokenPlatform,
            registrationCoordinator = registrationCoordinator,
            pushServiceInstanceManager = PushServiceInstanceManagerImpl
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyComposeAppTheme {
                SampleAppScreen(
                    registrationCoordinator = registrationCoordinator,
                    cardListViewModel = cardListViewModel,
                    settingsViewModel = settingsViewModel,
                    showResetDialog = showResetDialog,
                    onOpenResetDialog = { showResetDialog = true },
                    onDismissResetDialog = { showResetDialog = false },
                    onConfirmReset = { resetTokenPlatformAndRestart() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registrationCoordinator.onAppResumed()
    }

    private fun resetTokenPlatformAndRestart() {
        showResetDialog = false

        try {
            Log.d(TAG, "Deleting token platform data...")
            tokenPlatform.delete(null)
            Log.d(TAG, "Token platform data deleted successfully")
        } catch (exception: Exception) {
            Log.e(TAG, "Error deleting token platform", exception)
        }

        finish()
        Handler(Looper.getMainLooper()).postDelayed({
            Process.killProcess(Process.myPid())
        }, 100)
    }
}
