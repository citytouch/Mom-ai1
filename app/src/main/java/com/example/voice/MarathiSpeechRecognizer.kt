package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarathiSpeechRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioVolume = MutableStateFlow(0f)
    val audioVolume: StateFlow<Float> = _audioVolume.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    // Transparent status regarding offline vs online speech engine
    val engineStatusDescription = "Google स्पीच सेवा (ऑनलाइन आधारित)"

    init {
        checkAvailability()
    }

    fun checkAvailability(): Boolean {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isAvailable.value = available
        return available
    }

    fun startListening() {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("या फोनवर स्पीच रेकग्निशन उपलब्ध नाही.")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _partialText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB typically -2 to 10 into 0..1
                        val normalized = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
                        _audioVolume.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioVolume.value = 0f
                    }

                    override fun onError(errorCode: Int) {
                        _isListening.value = false
                        _audioVolume.value = 0f
                        val message = when (errorCode) {
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "काही स्पष्ट ऐकू आले नाही, कृपया पुन्हा स्पष्ट बोला."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "काही वेळ शांतता होती, कृपया पुन्हा 'बोला' वर दाबा."
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "इंटरनेट जोडणी तपासा. आवाज ओळखण्यासाठी नेटवर्क आवश्यक आहे."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "मायक्रोफोन वापरण्याची परवानगी आवश्यक आहे."
                            SpeechRecognizer.ERROR_AUDIO ->
                                "मायक्रोफोन ऑडिओ समस्या. कृपया पुन्हा प्रयत्न करा."
                            SpeechRecognizer.ERROR_SERVER ->
                                "स्पीच सर्व्हर एरर. कृपया थोड्या वेळाने प्रयत्न करा."
                            else ->
                                "आवाज ओळखता आला नाही ($errorCode), कृपया पुन्हा प्रयत्न करा."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioVolume.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _partialText.value = text
                            onResult(text)
                        } else {
                            onError("काही ऐकू आले नाही, कृपया पुन्हा बोला.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _partialText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "mr-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("mr-IN", "mr", "hi-IN", "en-IN"))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true

        } catch (e: Exception) {
            _isListening.value = false
            onError("मायक्रोफोन सुरू करता आला नाही: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _isListening.value = false
        _audioVolume.value = 0f
    }

    fun destroy() {
        stopListening()
    }
}
