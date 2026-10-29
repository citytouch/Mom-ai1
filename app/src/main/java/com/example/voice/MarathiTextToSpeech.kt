package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MarathiTextToSpeech(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isMarathiSupported = MutableStateFlow(false)
    val isMarathiSupported: StateFlow<Boolean> = _isMarathiSupported.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _statusNote = MutableStateFlow("आवाज प्रणाली सुरू होत आहे...")
    val statusNote: StateFlow<String> = _statusNote.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            val marathiLocale = Locale("mr", "IN")
            val availability = ttsEngine.isLanguageAvailable(marathiLocale)

            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                ttsEngine.language = marathiLocale
                _isMarathiSupported.value = true
                _statusNote.value = "मराठी आवाज उपलब्ध आहे."
            } else {
                // Check general Marathi locale
                val generalMr = Locale("mr")
                val genAvail = ttsEngine.isLanguageAvailable(generalMr)
                if (genAvail >= TextToSpeech.LANG_AVAILABLE) {
                    ttsEngine.language = generalMr
                    _isMarathiSupported.value = true
                    _statusNote.value = "मराठी आवाज उपलब्ध आहे."
                } else {
                    // Fallback to Hindi or default Indian English if Marathi voice pack is missing on phone
                    val hindiLocale = Locale("hi", "IN")
                    if (ttsEngine.isLanguageAvailable(hindiLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        ttsEngine.language = hindiLocale
                        _isMarathiSupported.value = false
                        _statusNote.value = "मराठी व्हॉइस डेटा उपलब्ध नाही (हिंदी ध्वनी पर्यायी वापरत आहे)."
                    } else {
                        ttsEngine.language = Locale.getDefault()
                        _isMarathiSupported.value = false
                        _statusNote.value = "मराठी व्हॉइस डेटा डाउनलोड करणे आवश्यक असू शकते."
                    }
                }
            }

            ttsEngine.setPitch(1.0f)
            ttsEngine.setSpeechRate(0.9f) // Slightly slower, clear rate for mother / seniors!

            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }
            })

            _isReady.value = true
        } else {
            _statusNote.value = "स्पीच इंजिन सुरू होऊ शकले नाही."
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!_isReady.value || text.isBlank()) {
            onDone?.invoke()
            return
        }
        val ttsEngine = tts ?: return
        onSpeechDoneCallback = onDone
        _isSpeaking.value = true
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mom_ai_${System.currentTimeMillis()}")
        }
        ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "mom_ai_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        onSpeechDoneCallback = null
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
