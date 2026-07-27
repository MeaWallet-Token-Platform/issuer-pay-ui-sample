package com.paymentology.dxp.issuerpay.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meawallet.mtp.MeaCard
import com.paymentology.dxp.issuerpay.ui.compose.PaymentFlowActivity
import com.paymentology.dxp.issuerpay.ui.compose.platform.TokenPlatform
import com.paymentology.dxp.issuerpay.ui.compose.PaymentActivityEvent

@Composable
fun CardListScreen(
    tokenPlatform: TokenPlatform,
    modifier: Modifier = Modifier
) {
    var cards by remember { mutableStateOf<List<MeaCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCard by remember { mutableStateOf<MeaCard?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var defaultCard by remember { mutableStateOf<MeaCard?>(null) }
    val context = LocalContext.current

    fun refreshCards() {
        isLoading = true
        try {
            cards = tokenPlatform.getCards()
            defaultCard = tokenPlatform.getDefaultCardForContactlessPayments()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load cards"
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshCards()
    }

    if (showActionDialog && selectedCard != null) {
        CardActionDialog(
            card = selectedCard!!,
            isDefault = defaultCard?.id == selectedCard?.id,
            onDismiss = { showActionDialog = false },
            onSetAsDefault = {
                selectedCard?.setAsDefaultForContactlessPayments()
                showActionDialog = false
                refreshCards()
            },
            onDelete = {
                selectedCard?.markForDeletion(null)
                showActionDialog = false
                refreshCards()
            },
            onTapAndPay = {
                selectedCard?.let { card ->
                    val intent = com.paymentology.dxp.issuerpay.ui.compose.common.PaymentActivityIntent(
                        context,
                        PaymentFlowActivity::class.java,
                        PaymentActivityEvent.PayByChosenCard(card.id)
                    )
                    context.startActivity(intent)
                }
                showActionDialog = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            cards.isEmpty() -> {
                Text(
                    text = "No cards available",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cards) { card ->
                        PaymentCardItem(
                            card = card,
                            isDefault = defaultCard?.id == card.id,
                            onClick = {
                                selectedCard = card
                                showActionDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCardItem(
    card: MeaCard,
    isDefault: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Card aspect ratio is typically 1.586:1 (85.6mm x 53.98mm)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A), // Deep blue
                            Color(0xFF3B82F6)  // Lighter blue
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Default card badge
            if (isDefault) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700), // Gold color
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "DEFAULT",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section - Payment Network
                Text(
                    text = getPaymentNetworkName(card),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Middle section - Card ID (masked for visual appeal)
                Text(
                    text = formatCardId(card.id),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom section - Status and Tokens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Status",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = getDigitizationStatus(card),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Tokens",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = getRemainingTokens(card),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun getPaymentNetworkName(card: MeaCard): String {
    return try {
        card.paymentNetwork?.name ?: "Unknown"
    } catch (_: Exception) {
        "Unknown"
    }
}

private fun formatCardId(cardId: String): String {
    // Format card ID to look like a card number
    return if (cardId.length > 16) {
        cardId.take(16).chunked(4).joinToString(" ")
    } else {
        cardId.chunked(4).joinToString(" ")
    }
}

private fun getDigitizationStatus(card: MeaCard): String {
    return try {
        card.state?.name ?: "Unknown"
    } catch (_: Exception) {
        "Unknown"
    }
}

private fun getRemainingTokens(card: MeaCard): String {
    return try {
        card.transactionCredentialsCount?.toString() ?: "N/A"
    } catch (_: Exception) {
        "N/A"
    }
}

@Composable
fun CardActionDialog(
    card: MeaCard,
    isDefault: Boolean,
    onDismiss: () -> Unit,
    onSetAsDefault: () -> Unit,
    onDelete: () -> Unit,
    onTapAndPay: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Card Actions")
        },
        text = {
            Column {
                Text(
                    text = "Card: ${formatCardId(card.id)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onTapAndPay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tap & Pay")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!isDefault) {
                    Button(
                        onClick = onSetAsDefault,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as Default")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "This is your default card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Card")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
