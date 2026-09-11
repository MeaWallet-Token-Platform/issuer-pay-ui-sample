package com.paymentology.dxp.issuerpay.sample.di

import android.content.Context
import com.paymentology.dxp.issuerpay.sample.SampleApp
import com.paymentology.dxp.issuerpay.sample.issuerpay.messaging.PushServiceInstanceManagerImpl
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationCoordinator
import com.paymentology.dxp.issuerpay.ui.compose.core.api.InitializationHelper

import com.paymentology.dxp.issuerpay.ui.compose.core.api.MeaTokenPlatformAdapter
import kotlin.getValue

/**
 * Default [AppContainer] implementation for the sample app process.
 *
 * Owns long-lived SDK dependencies for the sample app process.
 */
class AppContainerImpl(
    private val appContext: Context
) : AppContainer {
    private val sampleApp: SampleApp
        get() = appContext.applicationContext as SampleApp

    override val tokenPlatform by lazy { MeaTokenPlatformAdapter() }

    override val initializationHelper: InitializationHelper by lazy {
        InitializationHelper(appContext, tokenPlatform)
    }

    override val registrationCoordinator: RegistrationCoordinator by lazy {
        RegistrationCoordinator(
            appContext = appContext,
            tokenPlatform = tokenPlatform,
            initializationHelper = initializationHelper,
            pushServiceInstanceManager = PushServiceInstanceManagerImpl
        )
    }
}
