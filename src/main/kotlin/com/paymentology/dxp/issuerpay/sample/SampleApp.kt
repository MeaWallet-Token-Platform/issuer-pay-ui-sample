package com.paymentology.dxp.issuerpay.sample

import android.app.Application
import com.paymentology.dxp.issuerpay.sample.issuerpay.configureCustomUi
import com.paymentology.dxp.issuerpay.sample.di.AppContainer
import com.paymentology.dxp.issuerpay.sample.di.AppContainerImpl
import com.paymentology.dxp.issuerpay.sample.di.AppContainerProvider

/**
 * Sample application class for the Issuer Pay UI Sample Application.
 *
 * This class extends IssuerPayApp to automate initialization, basic setup and provide utilities
 * for easier start.
 * It also exposes an application-level dependency container for activities and services.
 *
 * It is possible to manually perform initialization and setup by extending Application class
 * instead of IssuerPayApp. Please, refer to the documentation for more details on how to do that.
 * https://developer.meawallet.com/mtp/sdk/implementation-guide
 */
//class SampleApp : IssuerPayApp(), AppContainerProvider {
class SampleApp : Application(), AppContainerProvider {
    override val appContainer: AppContainer by lazy { AppContainerImpl(this) }

    override fun onCreate() {
        super.onCreate()

        // Initialize the platform and SDK. This is required for the SDK to work properly.
        appContainer.initializationHelper.initializePlatform()


//        configureCustomUi()  // TODO: enable after custom UI is ready (if necessary)
    }
}