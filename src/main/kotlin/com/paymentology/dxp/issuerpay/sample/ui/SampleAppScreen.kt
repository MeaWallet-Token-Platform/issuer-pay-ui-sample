package com.paymentology.dxp.issuerpay.sample.ui

import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.paymentology.dxp.issuerpay.sample.R
import com.paymentology.dxp.issuerpay.ui.compose.platform.TokenPlatform
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppScreen(
    tokenPlatform: TokenPlatform
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

    if (showResetDialog.value) {
        AlertDialog(
            onDismissRequest = { showResetDialog.value = false },
            title = { Text("Reset Token Platform") },
            text = { Text("This will delete all cards and data, and restart the app. Are you sure?") },
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
                    Text("Reset & Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog.value = false }) {
                    Text("Cancel")
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
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset Token Platform") },
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
                        text = { Text(tab.displayName) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    DemoTab.Digitize -> DigitizeFlowScreen(modifier = Modifier.fillMaxSize())
                    DemoTab.Cards -> CardListScreen(
                        tokenPlatform = tokenPlatform,
                        modifier = Modifier.fillMaxSize()
                    )
                    DemoTab.Settings -> SettingsScreen(
                        tokenPlatform = tokenPlatform,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

enum class DemoTab(val displayName: String) {
    Digitize("Digitize"),
    Cards("Cards"),
    Settings("Settings")
}
