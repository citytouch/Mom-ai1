package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MicActiveRed
import com.example.ui.theme.TealPrimary

@Composable
fun VoiceMicButton(
    isListening: Boolean,
    volumeLevel: Float,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeColor = if (isListening) MicActiveRed else TealPrimary
    val dynamicScale = if (isListening) {
        pulseScale + (volumeLevel * 0.15f)
    } else 1f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .semantics {
                    contentDescription = if (isListening) "आवाज रेकॉर्डिंग थांबवा" else "मराठीत बोलण्यासाठी दाबा"
                }
        ) {
            // Outer subtle ripple circle when active
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(dynamicScale)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.22f))
                )
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .scale(dynamicScale * 0.95f)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.35f))
                )
            }

            // Main central large button
            Surface(
                shape = CircleShape,
                color = activeColor,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        role = Role.Button,
                        onClick = onClick
                    )
                    .testTag("main_mic_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isListening) "थांबवा" else "बोला",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator label below the button
        Text(
            text = when {
                isListening -> "मी ऐकत आहे... तुमचे वाक्य बोला"
                isSpeaking -> "आई AI बोलत आहे..."
                else -> "बोलण्यासाठी वरील बटणावर दाबा"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isListening) MicActiveRed else MaterialTheme.colorScheme.onSurface
        )
    }
}
