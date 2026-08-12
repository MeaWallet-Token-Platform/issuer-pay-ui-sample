package com.paymentology.dxp.issuerpay.sample.di

/**
 * Contract for application classes that expose the sample app [AppContainer].
 */
interface AppContainerProvider {
    /**
     * Shared dependency container bound to the application instance.
     */
    val appContainer: AppContainer
}
