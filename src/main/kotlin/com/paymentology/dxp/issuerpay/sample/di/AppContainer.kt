package com.paymentology.dxp.issuerpay.sample.di

import com.paymentology.dxp.issuerpay.ui.compose.core.api.InitializationHelper
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationCoordinator


/**
 * Root dependency container for the Issuer Pay UI sample app.
 *
 * Exposes long-lived SDK services shared across activities, services, and UI flows.
 */
interface AppContainer {
    /**
     * Token Platform facade used by screens and background services.
     */
    val tokenPlatform: TokenPlatform

    /**
     * Helper responsible for SDK initialization and setup.
     */
    val initializationHelper: InitializationHelper

    /**
     * App-scoped coordinator for wallet registration retries and state.
     */
    val registrationCoordinator: RegistrationCoordinator
}
