package dev.percym.yara.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.percym.yara.data.StoreCategory
import dev.percym.yara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: StoreCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(StoreCategory.GROCERY) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                .padding(bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Add Product",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            // Product name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text("Product name", color = TextMuted)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CheckboxBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldPrimary,
                    focusedContainerColor = PurpleMid,
                    unfocusedContainerColor = PurpleMid,
                )
            )

            // Category label
            Text(
                text = "Where to buy it?",
                style = MaterialTheme.typography.titleMedium.copy(color = TextSubtle)
            )

            // Category grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreCategory.entries.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { category ->
                            CategoryChip(
                                category = category,
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number, fill remaining space
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Add button
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name.trim(), selectedCategory) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = PurpleDark,
                    disabledContainerColor = GoldPrimary.copy(alpha = 0.4f),
                    disabledContentColor = PurpleDark.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Add to list",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: StoreCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) GoldPrimary.copy(alpha = 0.15f) else PurpleMid
    val borderColor = if (selected) GoldPrimary else CheckboxBorder.copy(alpha = 0.5f)
    val textColor = if (selected) GoldPrimary else TextSubtle

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = storeCategoryEmoji(category.name),
            fontSize = 20.sp
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium.copy(
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp
            ),
            maxLines = 1
        )
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
