package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistant.CommandResult
import com.example.assistant.MarathiCommandEngine
import com.example.data.AssistantMessage
import com.example.data.CallConfirmationState
import com.example.data.ContactItem
import com.example.data.LocalMomRepository
import com.example.data.MedicineReminder
import com.example.data.Sender
import com.example.data.VoiceState
import com.example.voice.MarathiSpeechRecognizer
import com.example.voice.MarathiTextToSpeech
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UiTab {
    object MainVoice : UiTab()
    object Contacts : UiTab()
    object Medicines : UiTab()
    object PrivacyInfo : UiTab()
}

sealed class PhoneCallAction {
    data class InitiateCall(val phoneNumber: String, val contactName: String) : PhoneCallAction()
}

class MomAiViewModel(application: Application) : AndroidViewModel(application) {

    val repository = LocalMomRepository(application)
    private val commandEngine = MarathiCommandEngine(repository)

    val textToSpeech = MarathiTextToSpeech(application)

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _currentTab = MutableStateFlow<UiTab>(UiTab.MainVoice)
    val currentTab: StateFlow<UiTab> = _currentTab.asSharedFlow().let { _currentTab.asStateFlow() }

    private val _callConfirmation = MutableStateFlow<CallConfirmationState>(CallConfirmationState.None)
    val callConfirmation: StateFlow<CallConfirmationState> = _callConfirmation.asStateFlow()

    private val _lastRecognizedSpeech = MutableStateFlow<String>("")
    val lastRecognizedSpeech: StateFlow<String> = _lastRecognizedSpeech.asStateFlow()

    private val _lastAiResponse = MutableStateFlow<String>("नमस्कार आई! काही विचारण्यासाठी खालील 'बोला' बटणावर दाबा.")
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    private val _messages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                id = "welcome",
                sender = Sender.MOM_AI,
                text = "नमस्कार आई! मी तुमची आई AI सहाय्यक आहे. वेळ, आजचा वार, तुमची औषधे किंवा फोन लावण्यासाठी 'बोला' दाबा."
            )
        )
    )
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    private val _phoneCallEvent = MutableSharedFlow<PhoneCallAction>()
    val phoneCallEvent: SharedFlow<PhoneCallAction> = _phoneCallEvent.asSharedFlow()

    private val speechRecognizer = MarathiSpeechRecognizer(
        context = application,
        onResult = { recognizedText ->
            handleSpeechInput(recognizedText)
        },
        onError = { errorMessage ->
            _voiceState.value = VoiceState.Error(errorMessage)
            textToSpeech.speak(errorMessage)
        }
    )

    val isListening: StateFlow<Boolean> = speechRecognizer.isListening
    val audioVolume: StateFlow<Float> = speechRecognizer.audioVolume
    val partialText: StateFlow<String> = speechRecognizer.partialText
    val isTtsSpeaking: StateFlow<Boolean> = textToSpeech.isSpeaking
    val isMarathiTtsAvailable: StateFlow<Boolean> = textToSpeech.isMarathiSupported
    val ttsStatusNote: StateFlow<String> = textToSpeech.statusNote

    fun switchTab(tab: UiTab) {
        _currentTab.value = tab
    }

    fun onMicButtonClicked() {
        triggerHaptic()
        if (speechRecognizer.isListening.value) {
            speechRecognizer.stopListening()
            _voiceState.value = VoiceState.Idle
        } else {
            textToSpeech.stop()
            _voiceState.value = VoiceState.Listening(0f)
            speechRecognizer.startListening()
        }
    }

    fun onTextCommandSubmitted(text: String) {
        if (text.isBlank()) return
        handleSpeechInput(text)
    }

    private fun handleSpeechInput(rawText: String) {
        val cleanText = rawText.trim()
        if (cleanText.isBlank()) return

        _lastRecognizedSpeech.value = cleanText
        _voiceState.value = VoiceState.Processing(cleanText)

        // Add user message to conversation list
        _messages.value = _messages.value + AssistantMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.USER,
            text = cleanText
        )

        val pendingContact = (_callConfirmation.value as? CallConfirmationState.Pending)?.contact
        val result = commandEngine.processCommand(cleanText, pendingContact)

        when (result) {
            is CommandResult.RequestCallConfirmation -> {
                _callConfirmation.value = CallConfirmationState.Pending(
                    contact = result.contact,
                    promptTextMarathi = result.promptMarathi
                )
                _lastAiResponse.value = result.promptMarathi
                addAiMessage(result.promptMarathi)
                _voiceState.value = VoiceState.Speaking(result.promptMarathi)
                textToSpeech.speak(result.promptMarathi)
            }

            is CommandResult.ConfirmCall -> {
                val contact = result.contact
                _callConfirmation.value = CallConfirmationState.None
                val msg = "${contact.nameMarathi} ला फोन लावत आहे..."
                _lastAiResponse.value = msg
                addAiMessage(msg)
                _voiceState.value = VoiceState.Speaking(msg)
                textToSpeech.speak(msg) {
                    viewModelScope.launch {
                        _phoneCallEvent.emit(
                            PhoneCallAction.InitiateCall(
                                phoneNumber = contact.phoneNumber,
                                contactName = contact.nameMarathi
                            )
                        )
                    }
                }
            }

            is CommandResult.CancelCall -> {
                _callConfirmation.value = CallConfirmationState.None
                val msg = "फोन लावणे रद्द केले आहे."
                _lastAiResponse.value = msg
                addAiMessage(msg)
                _voiceState.value = VoiceState.Speaking(msg)
                textToSpeech.speak(msg)
            }

            is CommandResult.SpeakResponse -> {
                _lastAiResponse.value = result.responseMarathi
                addAiMessage(result.responseMarathi)
                _voiceState.value = VoiceState.Speaking(result.responseMarathi)
                textToSpeech.speak(result.responseMarathi)
            }
        }
    }

    fun confirmPendingCall() {
        val pending = _callConfirmation.value as? CallConfirmationState.Pending ?: return
        handleSpeechInput("हो")
    }

    fun cancelPendingCall() {
        val pending = _callConfirmation.value as? CallConfirmationState.Pending ?: return
        handleSpeechInput("नाही")
    }

    fun replayLastResponse() {
        val text = _lastAiResponse.value
        if (text.isNotBlank()) {
            textToSpeech.speak(text)
        }
    }

    fun stopSpeaking() {
        textToSpeech.stop()
        if (_voiceState.value is VoiceState.Speaking) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun requestCallForContact(contact: ContactItem) {
        // Enforce the rule: State contact name and ask for explicit confirmation even when tapped from list!
        val prompt = "तुम्हाला ${contact.nameMarathi} ला फोन करायचा आहे का? हो किंवा नाही सांगा."
        _callConfirmation.value = CallConfirmationState.Pending(
            contact = contact,
            promptTextMarathi = prompt
        )
        _lastAiResponse.value = prompt
        addAiMessage(prompt)
        _voiceState.value = VoiceState.Speaking(prompt)
        textToSpeech.speak(prompt)
        _currentTab.value = UiTab.MainVoice
    }

    private fun addAiMessage(text: String) {
        _messages.value = _messages.value + AssistantMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.MOM_AI,
            text = text
        )
    }

    private fun triggerHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        textToSpeech.destroy()
    }
}
