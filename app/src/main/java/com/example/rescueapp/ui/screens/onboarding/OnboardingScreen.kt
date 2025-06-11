package com.example.rescueapp.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rescueapp.components.GradientButton
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: Brush
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Güvenli Konum Takibi",
            description = "Konumunuz sürekli olarak güvenli bir şekilde kaydedilir ve acil durumlarda otomatik olarak paylaşılır.",
            icon = Icons.Default.LocationOn,
            gradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            )
        ),
        OnboardingPage(
            title = "Tek Tuş Acil Durum",
            description = "Kaybolduğunuzda tek tuşla yardım çağırabilir, konumunuzu anında tüm acil durum kişilerinize gönderebilirsiniz.",
            icon = Icons.Default.Warning,
            gradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            )
        ),
        OnboardingPage(
            title = "Akıllı Yardım Listesi",
            description = "Güvenilir kişilerinizi rehberinizden seçin, acil durumlarda otomatik olarak detaylı bilgilendirme mesajları gönderilsin.",
            icon = Icons.Default.Person,
            gradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            )
        ),
        OnboardingPage(
            title = "Gelişmiş Rota Planlama",
            description = "Gideceğiniz yerleri harita üzerinde planlayın, kaybolma durumunda bu rotalar da acil durum kişilerinize gönderilsin.",
            icon = Icons.Default.LocationOn,
            gradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                )
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Animation for floating elements
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🆘",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "RescueMe",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Güvenliğiniz bizim önceliğimiz",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    floatingOffset = floatingOffset
                )
            }

            // Bottom section with indicators and navigation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index

                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        }
                                    )
                            )

                            if (index < pages.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Geri")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Progress text
                        Text(
                            "${pagerState.currentPage + 1} / ${pages.size}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        // Next/Finish button
                        GradientButton(
                            text = if (pagerState.currentPage == pages.size - 1) "Başla" else "İleri",
                            onClick = {
                                if (pagerState.currentPage == pages.size - 1) {
                                    onNavigateToHome()
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            icon = if (pagerState.currentPage == pages.size - 1)
                                Icons.Default.Check else Icons.Default.ArrowForward
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    floatingOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        Card(
            modifier = Modifier
                .size(120.dp)
                .offset(y = floatingOffset.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(page.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 24.sp
        )
    }
}
