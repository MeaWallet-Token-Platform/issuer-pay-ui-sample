package com.paymentology.dxp.issuerpay.sample.sdk

import com.meawallet.mtp.MeaCard
import com.meawallet.mtp.MeaCardReplenishListener
import com.meawallet.mtp.MeaCardState
import com.meawallet.mtp.MeaDigitizedCardStateChangeListener
import com.meawallet.mtp.MeaError
import com.meawallet.mtp.MeaTokenPlatform

object SdkCardEvents {
    private val updates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        extraBufferCapacity = 32,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    val cardUpdates: kotlinx.coroutines.flow.SharedFlow<Unit> = updates

    fun notifyUpdate() {
        updates.tryEmit(Unit)
    }
}

object SdkCardEventSubscriptions {
    private var areEventSubscriptionsActive = false

    private val replenishListener = object : MeaCardReplenishListener {
        override fun onReplenishCompleted(meaCard: MeaCard, numberOfPaymentTokens: Int) {
            SdkCardEvents.notifyUpdate()
        }

        override fun onReplenishFailed(meaCard: MeaCard, error: MeaError) {
            SdkCardEvents.notifyUpdate()
        }
    }

    private val stateChangeListener = object : MeaDigitizedCardStateChangeListener {
        override fun onStateChanged(card: MeaCard, newState: MeaCardState) {
            SdkCardEvents.notifyUpdate()
        }
    }

    @Synchronized
    fun subscribeToCardEvents() {
        if (areEventSubscriptionsActive) {
            return
        }

        MeaTokenPlatform.setCardReplenishListener(replenishListener)
        MeaTokenPlatform.setDigitizedCardStateChangeListener(stateChangeListener)
        areEventSubscriptionsActive = true
    }

    @Synchronized
    fun unsubscribeFromCardEvents() {
        if (!areEventSubscriptionsActive) {
            return
        }

        MeaTokenPlatform.removeCardReplenishListener()
        MeaTokenPlatform.removeDigitizedCardStateChangeListener()
        areEventSubscriptionsActive = false
    }
}
