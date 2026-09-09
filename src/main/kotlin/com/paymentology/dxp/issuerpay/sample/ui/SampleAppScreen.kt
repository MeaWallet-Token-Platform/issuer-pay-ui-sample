package com.paymentology.dxp.issuerpay.sample.ui

import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.sample.sdk.RegistrationCoordinator
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardListEffect
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardListIntent
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardListViewModel
import com.paymentology.dxp.issuerpay.ui.compose.core.api.TokenPlatform
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.SettingsViewModel
import com.paymentology.dxp.issuerpay.ui.compose.payment.api.PayByCardContract
import com.paymentology.dxp.issuerpay.ui.compose.payment.api.PayByCardLauncherInput
import com.paymentology.dxp.issuerpay.ui.compose.payment.api.PayByCardResult
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.SettingsIntent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppScreen(
    tokenPlatform: TokenPlatform,
    registrationCoordinator: RegistrationCoordinator,
    cardListViewModel: CardListViewModel,
    settingsViewModel: SettingsViewModel
) {
    val tabs = listOf(
        DemoTab.Digitize,
        DemoTab.Cards,
        DemoTab.Settings
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val showMenu = remember { mutableStateOf(false) }
    val showResetDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cardState by cardListViewModel.state.collectAsState()
    val settingsState by settingsViewModel.state.collectAsState()
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = PayByCardContract(),
        onResult = { result ->
            when (result) {
                is PayByCardResult.Success -> {
                    Log.d("SampleAppScreen", "Payment successful. Transaction data: ${result.paymentData}")
                }
                is PayByCardResult.Error -> {
                    Log.e("SampleAppScreen", "Payment failed: ${result.error}")
                }
                is PayByCardResult.Cancelled -> {
                    Log.d("SampleAppScreen", "Payment cancelled by user")
                }
            }
            cardListViewModel.dispatch(CardListIntent.Refresh)
        }
    )

    LaunchedEffect(cardListViewModel) {
        cardListViewModel.effects.collect { effect ->
            when (effect) {
                is CardListEffect.LaunchPayment -> {
                    paymentLauncher.launch(PayByCardLauncherInput(cardId = effect.cardId))
                }
            }
        }
    }

    if (showResetDialog.value) {
        AlertDialog(
            onDismissRequest = { showResetDialog.value = false },
            title = { Text(stringResource(R.string.ui_reset_token_platform)) },
            text = { Text(stringResource(R.string.ui_reset_token_platform_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog.value = false
                        try {
                            Log.d("SampleAppScreen", "Deleting token platform data...")
                            tokenPlatform.delete(null)
                            Log.d("SampleAppScreen", "Token platform data deleted successfully")
                        } catch (e: Exception) {
                            Log.e("SampleAppScreen", "Error deleting token platform", e)
                        }

                        if (context is ComponentActivity) {
                            context.finish()
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            Process.killProcess(Process.myPid())
                        }, 100)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.ui_reset_token_platform_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog.value = false }) {
                    Text(stringResource(R.string.ui_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showMenu.value = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.ui_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ui_reset_token_platform_menu_item)) },
                            onClick = {
                                showMenu.value = false
                                showResetDialog.value = true
                            }
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    DemoTab.Digitize -> DigitizeFlowScreen(
                        registrationCoordinator = registrationCoordinator,
                        modifier = Modifier.fillMaxSize()
                    )
                    DemoTab.Cards -> CardListScreen(
                        state = cardState,
                        onRefresh = { cardListViewModel.dispatch(CardListIntent.Refresh) },
                        onCardClicked = { cardId -> cardListViewModel.dispatch(CardListIntent.CardClicked(cardId)) },
                        onDismissDialog = { cardListViewModel.dispatch(CardListIntent.DismissDialog) },
                        onSetDefaultSelectedCard = { cardListViewModel.dispatch(CardListIntent.SetDefaultSelectedCard) },
                        onDeleteSelectedCard = { cardListViewModel.dispatch(CardListIntent.DeleteSelectedCard) },
                        onTapAndPaySelectedCard = { cardListViewModel.dispatch(CardListIntent.TapAndPaySelectedCard) },
                        modifier = Modifier.fillMaxSize()
                    )
                    DemoTab.Settings -> SettingsScreen(
                        state = settingsState,
                        onRefresh = { settingsViewModel.dispatch(SettingsIntent.Refresh) },
                        onSetDefaultPaymentApp = { activity ->
                            settingsViewModel.dispatch(SettingsIntent.SetDefaultPaymentApp(activity))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

enum class DemoTab(@param:androidx.annotation.StringRes val labelRes: Int) {
    Digitize(R.string.ui_digitize_tab),
    Cards(R.string.ui_cards_tab),
    Settings(R.string.ui_settings_tab)
}
