package dev.percym.yara.ui.products

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.percym.yara.data.Product
import dev.percym.yara.data.StoreCategory
import dev.percym.yara.ui.theme.*

@Composable
@Preview
fun ProductsScreen(
    viewModel: ProductsViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var showNearbySheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleDark, PurpleMid, PurpleDark)
                )
            )
    ) {
        // Bokeh orbs at top
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            drawGlowOrb(Offset(size.width * 0.8f, size.height * 0.4f), 120f, Color(0xFFF0B429))
            drawGlowOrb(Offset(size.width * 0.15f, size.height * 0.6f), 80f, Color(0xFFF0B429))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Shopping List",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showNearbySheet = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "Nearby shops",
                            tint = GoldPrimary
                        )
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PurpleCard)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Sign out",
                            tint = TextSubtle
                        )
                    }
                }
            }

            // Active reminder summary card (gold gradient — inspired by the prayer time card)
            ActiveReminderCard(productCount = products.size)

            Spacer(Modifier.height(20.dp))

            // Section label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                )
                Text(
                    text = "${products.size} total",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                )
            }

            Spacer(Modifier.height(12.dp))

            if (products.isEmpty() && !isLoading) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 24.dp, end = 24.dp,
                        bottom = 100.dp  // room for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }
        }

        // Gold FAB
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp)
                .size(60.dp),
            shape = CircleShape,
            containerColor = GoldPrimary,
            contentColor = PurpleDark
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add product", modifier = Modifier.size(28.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = GoldPrimary
            )
        }
    }

    if (showNearbySheet) {
        NearbyShopsSheet(onDismiss = { showNearbySheet = false })
    }

    if (showAddSheet) {
        AddProductSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { name, category ->
                viewModel.addProduct(name, category)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun ActiveReminderCard(productCount: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFC87A00), GoldPrimary, Color(0xFFF5C450))
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (productCount == 0) "No items yet" else "Location Reminders",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PurpleDark.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = if (productCount == 0) "Add items below" else "$productCount item${if (productCount != 1) "s" else ""} tracked",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PurpleDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Active · Vibrates near shops",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PurpleDark.copy(alpha = 0.6f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PurpleDark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PurpleDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PurpleCard.copy(alpha = 0.6f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No items yet", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
            Text(
                "Tap + to add a product. You'll get a\nvibration alert near shops that sell it.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSubtle),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ProductCard(product: Product, onDelete: () -> Unit) {
    val categoryLabel = try {
        StoreCategory.valueOf(product.category).displayName
    } catch (_: IllegalArgumentException) { product.category }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PurpleCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle checkbox (inspired by the checklist items in design)
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, CheckboxBorder, CircleShape)
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
            )
        }

        // Category pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PurpleMid)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = storeCategoryEmoji(product.category),
                style = MaterialTheme.typography.labelMedium.copy(color = TextSubtle),
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = ErrorRed.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun storeCategoryEmoji(category: String) = when (category) {
    "GROCERY"     -> "🛒"
    "PHARMACY"    -> "💊"
    "BAKERY"      -> "🥖"
    "HARDWARE"    -> "🔧"
    "CONVENIENCE" -> "🏪"
    else          -> "🛍"
}

private fun DrawScope.drawGlowOrb(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0f)),
            center = center, radius = radius * 2.8f
        ),
        radius = radius * 2.8f, center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.05f)),
            center = center, radius = radius
        ),
        radius = radius, center = center
    )
}
