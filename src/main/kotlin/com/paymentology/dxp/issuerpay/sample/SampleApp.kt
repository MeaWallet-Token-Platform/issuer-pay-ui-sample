package com.paymentology.dxp.issuerpay.sample

import com.paymentology.dxp.issuerpay.sample.issuerpay.configureCustomUi
import com.paymentology.dxp.issuerpay.ui.compose.quickstart.IssuerPayApp

/**
 * Sample application class for the Issuer Pay UI Sample Application.
 *
 * This class extends IssuerPayApp to automate initialization, basic setup and provide utilities
 * for easier start.
 *
 * It is possible to manually perform initialization and setup by extending Application class
 * instead of IssuerPayApp. Please, refer to the documentation for more details on how to do that.
 * https://developer.meawallet.com/mtp/sdk/implementation-guide
 */
class SampleApp : IssuerPayApp() {

    override fun onCreate() {
        super.onCreate()
//        configureCustomUi()  // TODO: enable after custom UI is ready (if necessary)
    }
}