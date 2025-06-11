package com.example.rescueapp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), label = ""
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3500)
        onNavigateToOnboarding()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        Color(0xFF1A202C)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background particles
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawAnimatedBackground(rotationAngle)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Animated logo with pulse effect
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.7f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🆘",
                    fontSize = 72.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "RescueMe",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Güvenliğiniz bizim önceliğimiz",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Her an yanınızda",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced loading indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { index ->
                    val delay = index * 300
                    val animatedScale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ), label = ""
                    )

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(animatedScale)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAnimatedBackground(rotationAngle: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 2.5f

    // Draw rotating particles
    for (i in 0..8) {
        val angle = (rotationAngle + i * 40) * (Math.PI / 180)
        val x = centerX + cos(angle).toFloat() * radius
        val y = centerY + sin(angle).toFloat() * radius

        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = 15f + (i % 3) * 5f,
            center = Offset(x, y)
        )
    }

    // Inner rotating circles
    for (i in 0..5) {
        val angle = (-rotationAngle * 0.7f + i * 60) * (Math.PI / 180)
        val innerRadius = radius * 0.6f
        val x = centerX + cos(angle).toFloat() * innerRadius
        val y = centerY + sin(angle).toFloat() * innerRadius

        drawCircle(
            color = Color.White.copy(alpha = 0.1f),
            radius = 8f,
            center = Offset(x, y)
        )
    }
}
