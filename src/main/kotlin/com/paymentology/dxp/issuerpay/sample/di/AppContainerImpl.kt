package com.paymentology.dxp.issuerpay.sample.di

import android.content.Context
import com.paymentology.dxp.issuerpay.sample.SampleApp
import com.paymentology.dxp.issuerpay.ui.compose.core.api.InitializationHelper

import com.paymentology.dxp.issuerpay.ui.compose.core.api.IssuerPayApp
import com.paymentology.dxp.issuerpay.ui.compose.core.api.MeaTokenPlatformAdapter
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlin.getValue

/**
 * Default [AppContainer] implementation for the sample app process.
 *
 * Reuses SDK dependencies that are initialized by [IssuerPayApp].
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
}
