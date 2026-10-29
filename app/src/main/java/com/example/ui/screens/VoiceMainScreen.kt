package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallConfirmationState
import com.example.ui.MomAiViewModel
import com.example.ui.components.CallConfirmationCard
import com.example.ui.components.VoiceMicButton
import com.example.ui.theme.SaffronLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceMainScreen(
    viewModel: MomAiViewModel,
    onOpenPrivacyInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsState()
    val audioVolume by viewModel.audioVolume.collectAsState()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val lastSpeech by viewModel.lastRecognizedSpeech.collectAsState()
    val lastResponse by viewModel.lastAiResponse.collectAsState()
    val callConfirmation by viewModel.callConfirmation.collectAsState()

    var showTextInputDialog by remember { mutableStateOf(false) }
    var textInputState by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Greeting Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "नमस्कार आई! 🙏",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "तुमची मराठी आवाज सहाय्यक",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Quick Info & Keyboard typing action
            Row {
                IconButton(
                    onClick = { showTextInputDialog = true },
                    modifier = Modifier.testTag("open_text_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "टाईप करून विचारा",
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onOpenPrivacyInfo,
                    modifier = Modifier.testTag("open_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "माहिती व गोपनीयता",
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System & Privacy Status Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TealPrimaryLight.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "आवाज ओळख: Google स्पीच सेवा (ऑनलाइन) • माहिती स्थानिक",
                    fontSize = 13.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // If Call Confirmation is pending: Show prominent confirmation card!
        when (val callState = callConfirmation) {
            is CallConfirmationState.Pending -> {
                CallConfirmationCard(
                    contact = callState.contact,
                    promptText = callState.promptTextMarathi,
                    onConfirm = { viewModel.confirmPendingCall() },
                    onCancel = { viewModel.cancelPendingCall() },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is CallConfirmationState.None -> {
                // Speech Recognition Feedback Cards
                if (lastSpeech.isNotBlank() || isListening) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SaffronLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = SaffronSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "तुम्ही म्हणालात:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SaffronSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isListening && partialText.isNotBlank()) partialText else lastSpeech,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // AI Response Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TealPrimaryLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "आई AI चे उत्तर:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TealPrimary
                                )
                            }

                            // Replay audio button
                            FilledTonalButton(
                                onClick = { viewModel.replayLastResponse() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White,
                                    contentColor = TealPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("replay_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "पुन्हा ऐका",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "पुन्हा ऐका",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = lastResponse,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // THE HERO BUTTON: Large Microphone Button labelled “बोला”
                VoiceMicButton(
                    isListening = isListening,
                    volumeLevel = audioVolume,
                    isSpeaking = isSpeaking,
                    onClick = { viewModel.onMicButtonClicked() },
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Common Marathi Suggestions Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "नेहमीचे मराठी प्रश्न (दाबूनही विचारू शकता):",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val quickPrompts = listOf(
                        "वेळ काय झाली?",
                        "आज कोणता वार आहे?",
                        "राहुलला फोन लावा",
                        "माझी औषधे",
                        "मदत करा",
                        "नमस्कार"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickPrompts.forEach { phrase ->
                            SuggestionChip(
                                onClick = { viewModel.onTextCommandSubmitted(phrase) },
                                label = {
                                    Text(
                                        text = phrase,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White,
                                    labelColor = TealPrimary
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = TealPrimary.copy(alpha = 0.4f),
                                    borderWidth = 1.5.dp
                                ),
                                modifier = Modifier.testTag("quick_chip_${phrase.replace(" ", "_")}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Text Input Dialog (for quiet environments or testing)
    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text(
                    text = "मराठीत प्रश्न लिहा",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "उदा. 'वेळ काय झाली', 'राहुलला फोन लावा', 'माझी औषधे'",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textInputState,
                        onValueChange = { textInputState = it },
                        label = { Text("येथे लिहा") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("text_input_field"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textInputState.isNotBlank()) {
                            viewModel.onTextCommandSubmitted(textInputState)
                            textInputState = ""
                            showTextInputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("विचारा")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("रद्द")
                }
            }
        )
    }
}
