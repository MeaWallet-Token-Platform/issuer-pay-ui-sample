package com.paymentology.dxp.issuerpay.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardUiModel
import com.paymentology.dxp.issuerpay.sample.ui.viewmodel.CardListUiState

@Composable
fun CardListScreen(
    state: CardListUiState,
    onRefresh: () -> Unit,
    onCardClicked: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onSetDefaultSelectedCard: () -> Unit,
    onDeleteSelectedCard: () -> Unit,
    onTapAndPaySelectedCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCard = state.selectedCard
    if (state.showActionDialog && selectedCard != null) {
        CardActionDialog(
            card = selectedCard,
            isDefault = selectedCard.isDefault,
            canSetAsDefault = selectedCard.isActive,
            canTapAndPay = selectedCard.isActive,
            onDismiss = onDismissDialog,
            onSetAsDefault = onSetDefaultSelectedCard,
            onDelete = onDeleteSelectedCard,
            onTapAndPay = onTapAndPaySelectedCard
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Refresh")
                    }
                }
            }

            state.cards.isEmpty() -> {
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
                    items(state.cards, key = { it.card.id }) { card ->
                        PaymentCardItem(
                            card = card,
                            onClick = { onCardClicked(card.card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCardItem(
    card: CardUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
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
                            Color(0xFF1E3A8A),
                            Color(0xFF3B82F6)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            if (card.isDefault) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700),
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

            if (!card.isActive) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = "Card is not active",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = card.paymentNetworkName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formatCardId(card.card.id),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                            text = card.statusText,
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
                            text = card.remainingTokensText,
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

@Composable
fun CardActionDialog(
    card: CardUiModel,
    isDefault: Boolean,
    canSetAsDefault: Boolean,
    canTapAndPay: Boolean,
    onDismiss: () -> Unit,
    onSetAsDefault: () -> Unit,
    onDelete: () -> Unit,
    onTapAndPay: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Card Actions") },
        text = {
            Column {
                Text(
                    text = "Card: ${formatCardId(card.card.id)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onTapAndPay,
                    enabled = canTapAndPay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tap & Pay")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isDefault) {
                    Text(
                        text = "This is your default card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else if (!canSetAsDefault) {
                    Text(
                        text = "This card is not active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Button(
                        onClick = onSetAsDefault,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as Default")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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

private fun formatCardId(cardId: String): String {
    return if (cardId.length > 16) {
        cardId.take(16).chunked(4).joinToString(" ")
    } else {
        cardId.chunked(4).joinToString(" ")
    }
}
