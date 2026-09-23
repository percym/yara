package dev.percym.yara.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import dev.percym.yara.auth.AuthManager
import dev.percym.yara.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleDark, PurpleMid, PurpleDark)
                )
            )
    ) {
        // Decorative golden bokeh orbs at top (inspired by glowing lanterns)
        GlowingOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top spacer to push content below orbs
            Spacer(Modifier.height(200.dp))

            // Central content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GoldGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "YaRA",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Daily Shopping\nReminder",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Add items to your list and get a vibration\nalert whenever you walk near a shop.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSubtle),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Bottom CTA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            authManager.signInWithGoogle()
                                .onSuccess { onLoginSuccess() }
                                .onFailure { errorMessage = it.message ?: "Sign-in failed" }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = PurpleDark,
                        disabledContainerColor = GoldPrimary.copy(alpha = 0.6f),
                        disabledContentColor = PurpleDark.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = PurpleDark
                        )
                    } else {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleDark
                            )
                        )
                    }
                }

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GlowingOrbs() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        drawGlowOrb(Offset(size.width * 0.25f, size.height * 0.5f), 130f, Color(0xFFF0B429))
        drawGlowOrb(Offset(size.width * 0.72f, size.height * 0.35f), 100f, Color(0xFFF0B429))
        drawGlowOrb(Offset(size.width * 0.55f, size.height * 0.75f), 80f,  Color(0xFFD48C10))
        drawGlowOrb(Offset(size.width * 0.1f,  size.height * 0.25f), 60f,  Color(0xFFF0B429))
    }
}

private fun DrawScope.drawGlowOrb(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0f)),
            center = center,
            radius = radius * 2.5f
        ),
        radius = radius * 2.5f,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.7f), color.copy(alpha = 0.1f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
