package dev.percym.yara.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.percym.yara.data.NearbyShop
import dev.percym.yara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyShopsSheet(
    onDismiss: () -> Unit,
    viewModel: ProductsViewModel = viewModel()
) {
    val shops by viewModel.nearbyShops.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingShops.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadNearbyShops() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = PurpleCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(CheckboxBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Shops",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "within 3 km",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                )
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.5.dp)
                            Text(
                                text = "Searching nearby shops…",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSubtle)
                            )
                        }
                    }
                }

                shops.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "No shops found nearby",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (error != null) ErrorRed else TextSubtle
                            )
                        )
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(shops, key = { it.id }) { shop ->
                            ShopRow(shop)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopRow(shop: NearbyShop) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PurpleMid)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GoldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = storeCategoryEmoji(shop.category.name),
                fontSize = 20.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
            if (shop.address.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = shop.address,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted),
                    maxLines = 1
                )
            }
        }

        Text(
            text = formatDistance(shop.distanceMeters),
            style = MaterialTheme.typography.labelMedium.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun formatDistance(meters: Float): String =
    if (meters < 1000) "${meters.toInt()} m" else "${"%.1f".format(meters / 1000)} km"

private fun storeCategoryEmoji(category: String) = when (category) {
    "GROCERY"     -> "🛒"
    "PHARMACY"    -> "💊"
    "BAKERY"      -> "🥖"
    "HARDWARE"    -> "🔧"
    "CONVENIENCE" -> "🏪"
    else          -> "🛍"
}
