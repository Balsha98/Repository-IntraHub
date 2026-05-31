package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bazovic.balsa.intrahub.ui.theme.OrangeRIT
import kotlinx.coroutines.delay

private val PHASES = listOf("Authenticating", "Loading your teams", "Syncing schedule")

@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rotation",
    )

    LaunchedEffect(Unit) {
        repeat(PHASES.size - 1) {
            delay(600)
            phase = it + 1
        }
        delay(700)
        onLoadingComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ─── SECTION: Spinner + Diamond Logo ─── //
        Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().rotate(rotation),
                color = OrangeRIT,
                strokeWidth = 3.dp,
                trackColor = OrangeRIT.copy(alpha = 0.2f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeRIT)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "IntraHub",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = (-0.66).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${PHASES[phase]}…",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
        )

        Spacer(Modifier.height(32.dp))

        // ─── SECTION: Progress Dots ─── //
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PHASES.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (i <= phase) OrangeRIT else Color.White.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(Modifier.height(80.dp))

        Text(
            "ROCHESTER INSTITUTE OF TECHNOLOGY",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.0.sp,
        )
    }
}//LoadingScreen
