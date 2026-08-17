package com.paymentology.dxp.issuerpay.sample.di

import android.content.Context

/**
 * Returns the sample app dependency container from application context.
 *
 * Requires application to implement [AppContainerProvider].
 */
val Context.appContainer: AppContainer
    get() = (applicationContext as AppContainerProvider).appContainer
