package com.paymentology.dxp.issuerpay.sample.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meawallet.mtp.MeaCard
import com.meawallet.mtp.MeaCardListener
import com.meawallet.mtp.MeaCardState
import com.meawallet.mtp.MeaError
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationCoordinator
import com.paymentology.dxp.issuerpay.sample.issuerpay.RegistrationState
import com.paymentology.dxp.issuerpay.sample.issuerpay.SdkCardEvents
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CardUiModel(
    val card: MeaCard,
    val isActive: Boolean,
    val isDefault: Boolean,
    val paymentNetworkName: String,
    val statusText: String,
    val remainingTokensText: String
)

data class CardListUiState(
    val isLoading: Boolean = true,
    val cards: List<CardUiModel> = emptyList(),
    val selectedCardId: String? = null,
    val showActionDialog: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedCard: CardUiModel? = cards.firstOrNull { it.card.id == selectedCardId }
}

sealed interface CardListIntent {
    data object Refresh : CardListIntent
    data class CardClicked(val cardId: String) : CardListIntent
    data object DismissDialog : CardListIntent
    data object SetDefaultSelectedCard : CardListIntent
    data object DeleteSelectedCard : CardListIntent
    data object TapAndPaySelectedCard : CardListIntent
    data object ClearError : CardListIntent
}

sealed interface CardListEffect {
    data class LaunchPayment(val cardId: String) : CardListEffect
}

class CardListViewModel(
    appContext: Context,
    private val tokenPlatform: TokenPlatform,
    private val registrationCoordinator: RegistrationCoordinator
) : ViewModel() {

    companion object {
        private const val TAG = "CardListViewModel"
    }

    private val applicationContext = appContext.applicationContext

    private val _state = MutableStateFlow(CardListUiState())
    val state: StateFlow<CardListUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CardListEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CardListEffect> = _effects.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        observeRegistrationState()
        observeCardEvents()
        dispatch(CardListIntent.Refresh)
    }

    fun dispatch(intent: CardListIntent) {
        when (intent) {
            CardListIntent.Refresh -> refreshCards()
            is CardListIntent.CardClicked -> selectCard(intent.cardId)
            CardListIntent.DismissDialog -> dismissDialog()
            CardListIntent.SetDefaultSelectedCard -> setSelectedCardAsDefault()
            CardListIntent.DeleteSelectedCard -> deleteSelectedCard()
            CardListIntent.TapAndPaySelectedCard -> launchSelectedCardPayment()
            CardListIntent.ClearError -> clearError()
        }
    }

    private fun observeRegistrationState() {
        viewModelScope.launch {
            registrationCoordinator.registrationState.collectLatest { state ->
                if (state == RegistrationState.Registered) {
                    dispatch(CardListIntent.Refresh)
                }
            }
        }
    }

    private fun observeCardEvents() {
        viewModelScope.launch {
            SdkCardEvents.cardUpdates.collectLatest {
                dispatch(CardListIntent.Refresh)
            }
        }
    }

    private fun refreshCards() {
        if (refreshJob?.isActive == true) {
            return
        }

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val sdkCards = tokenPlatform.getCards()
                    val defaultCardId = tokenPlatform.getDefaultCardForContactlessPayments()?.id
                    sdkCards.map { card ->
                        card.toUiModel(defaultCardId)
                    }
                }
            }

            result.fold(
                onSuccess = { cards ->
                    val selectedCardId = state.value.selectedCardId?.takeIf { selectedId ->
                        cards.any { it.card.id == selectedId }
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            cards = cards,
                            selectedCardId = selectedCardId,
                            showActionDialog = selectedCardId != null && it.showActionDialog,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    Log.e(TAG, "Failed to refresh cards.", throwable)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: applicationContext.getString(R.string.ui_failed_to_load_cards)
                        )
                    }
                }
            )

            refreshJob = null
        }
    }

    private fun selectCard(cardId: String) {
        _state.update {
            it.copy(
                selectedCardId = cardId,
                showActionDialog = true,
                errorMessage = null
            )
        }
    }

    private fun dismissDialog() {
        _state.update { it.copy(showActionDialog = false) }
    }

    private fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun setSelectedCardAsDefault() {
        val selectedCard = state.value.selectedCard
        if (selectedCard == null) {
            setError(applicationContext.getString(R.string.ui_no_card_selected))
            return
        }

        if (!selectedCard.isActive) {
            setError(applicationContext.getString(R.string.ui_set_as_default_active_only))
            return
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    selectedCard.card.setAsDefaultForContactlessPayments()
                }
            }

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Selected card set as default: ${selectedCard.card.id}")
                    _state.update { it.copy(showActionDialog = false, errorMessage = null) }
                    dispatch(CardListIntent.Refresh)
                },
                onFailure = { throwable ->
                    Log.e(TAG, "Failed to set card as default.", throwable)
                    setError(throwable.message ?: applicationContext.getString(R.string.ui_failed_to_set_card_default))
                    dispatch(CardListIntent.Refresh)
                }
            )
        }
    }

    private fun deleteSelectedCard() {
        val selectedCard = state.value.selectedCard
        if (selectedCard == null) {
            setError(applicationContext.getString(R.string.ui_no_card_selected))
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                selectedCard.card.delete(object : MeaCardListener {
                    override fun onSuccess(card: MeaCard) {
                        Log.d(TAG, "Deleted card: ${card.id}")
                        _state.update {
                            it.copy(
                                selectedCardId = null,
                                showActionDialog = false,
                                errorMessage = null
                            )
                        }
                        dispatch(CardListIntent.Refresh)
                    }

                    override fun onFailure(error: MeaError) {
                        Log.e(TAG, "Failed to delete card ${selectedCard.card.id}: ${error.message}")
                        setError(error.message ?: applicationContext.getString(R.string.ui_failed_to_delete_card))
                        dispatch(CardListIntent.Refresh)
                    }
                })
            }
        }
    }

    private fun launchSelectedCardPayment() {
        val selectedCard = state.value.selectedCard
        if (selectedCard == null) {
            setError(applicationContext.getString(R.string.ui_no_card_selected))
            return
        }

        if (!selectedCard.isActive) {
            setError(applicationContext.getString(R.string.ui_tap_and_pay_active_only))
            return
        }

        _state.update { it.copy(showActionDialog = false, errorMessage = null) }
        viewModelScope.launch {
            _effects.emit(CardListEffect.LaunchPayment(selectedCard.card.id))
        }
    }

    private fun setError(message: String) {
        _state.update {
            it.copy(
                errorMessage = message,
                showActionDialog = false
            )
        }
    }

    private fun MeaCard.toUiModel(defaultCardId: String?): CardUiModel {
        return CardUiModel(
            card = this,
            isActive = isActiveCard(this),
            isDefault = defaultCardId == id,
            paymentNetworkName = paymentNetworkName(),
            statusText = statusText(),
            remainingTokensText = remainingTokensText()
        )
    }

    private fun MeaCard.paymentNetworkName(): String {
        return try {
            paymentNetwork?.name ?: "Unknown"
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to read payment network for $id", exception)
            "Unknown"
        }
    }

    private fun MeaCard.statusText(): String {
        return try {
            state?.name ?: "Unknown"
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to read state for $id", exception)
            "Unknown"
        }
    }

    private fun MeaCard.remainingTokensText(): String {
        return try {
            transactionCredentialsCount?.toString() ?: "N/A"
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to read token count for $id", exception)
            "N/A"
        }
    }

    private fun isActiveCard(card: MeaCard): Boolean {
        return try {
            card.state == MeaCardState.ACTIVE
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to determine active state for ${card.id}", exception)
            false
        }
    }
}

class CardListViewModelFactory(
    private val appContext: Context,
    private val tokenPlatform: TokenPlatform,
    private val registrationCoordinator: RegistrationCoordinator
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CardListViewModel::class.java) -> {
                CardListViewModel(appContext, tokenPlatform, registrationCoordinator) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
