package com.paymentology.dxp.issuerpay.sample.issuerpay.messaging

import com.google.firebase.messaging.FirebaseMessaging
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PushServiceInstanceIdGetListener
import com.paymentology.dxp.issuerpay.ui.compose.core.api.PushServiceInstanceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

object PushServiceInstanceManagerImpl : PushServiceInstanceManager {

    private var tokenDataMerger: StateFlow<String>? = null
    private var localTokenKeeper: MutableStateFlow<String> = MutableStateFlow("")

    override fun getIdToken(onResultListener: PushServiceInstanceIdGetListener) {

        getFirebaseInstanceId(onResultListener)
    }

    override fun getObservableIdToken(coroutineScope: CoroutineScope): StateFlow<String> {
        val mergedStateFlow = tokenDataMerger ?: merge(
            MyFcmListenerService.getLastReceivedToken(),
            localTokenKeeper
        ).stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        ).also { tokenDataMerger = it }

        if (mergedStateFlow.value.isEmpty()) {
            getFirebaseInstanceId(null)
        }

        return mergedStateFlow
    }

    private fun getFirebaseInstanceId(onResultListener: PushServiceInstanceIdGetListener?) {

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful || task.result == null) {
                    onResultListener?.onFailure(kotlin.Exception("Failed to get Firebase Token"))
                    return@addOnCompleteListener
                }

                // Get Instance ID token
                val token: String = task.result
                onResultListener?.onSuccess(token)

                localTokenKeeper.value = token
            }
    }
}