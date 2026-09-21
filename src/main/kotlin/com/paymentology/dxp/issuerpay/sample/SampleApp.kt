package com.paymentology.dxp.issuerpay.sample

import android.app.Application
import com.paymentology.dxp.issuerpay.sample.di.AppContainer
import com.paymentology.dxp.issuerpay.sample.di.AppContainerImpl
import com.paymentology.dxp.issuerpay.sample.di.AppContainerProvider
import com.paymentology.dxp.issuerpay.sample.issuerpay.CardEventSubscriptions
import com.paymentology.dxp.issuerpay.ui.compose.core.api.IssuerPayUi

/**
 * Sample application class for the Issuer Pay UI Sample Application.
 *
 * This class exposes an application-level dependency container for activities and services.
 */
class SampleApp : Application(), AppContainerProvider {
    override val appContainer: AppContainer by lazy { AppContainerImpl(this) }

    override fun onCreate() {
        super.onCreate()

        // TODO: Initialize the platform and SDK. This is required for the SDK to work properly.
        IssuerPayUi.initialize(this)

        CardEventSubscriptions.subscribeToCardEvents()
        appContainer.registrationCoordinator.start()


//        configureCustomUi()  // TODO: enable after custom UI is ready (if necessary)
    }

    override fun onTerminate() {
        appContainer.registrationCoordinator.stop()
        CardEventSubscriptions.unsubscribeFromCardEvents()
        super.onTerminate()
    }
}