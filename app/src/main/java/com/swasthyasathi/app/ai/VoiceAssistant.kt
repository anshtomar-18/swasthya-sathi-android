package com.swasthyasathi.app.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class VoiceAssistant(private val context: Context) : TextToSpeech.OnInitListener {

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isVoiceRepliesEnabled = true
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        mainHandler.post { initSpeechRecognizer() }
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _voiceState.value = VoiceState.LISTENING
                            _errorMessage.value = null
                        }

                        override fun onBeginningOfSpeech() {
                            _voiceState.value = VoiceState.LISTENING
                        }

                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _voiceState.value = VoiceState.PROCESSING
                        }

                        override fun onError(error: Int) {
                            _voiceState.value = VoiceState.ERROR
                            _errorMessage.value = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking again."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out. Tap microphone to retry."
                                SpeechRecognizer.ERROR_NETWORK -> "Network issue. Reverting to system speech launcher."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                else -> "Speech recognition error ($error)"
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _voiceState.value = VoiceState.IDLE
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                _recognizedText.value = matches[0]
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                _recognizedText.value = matches[0]
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (e: Exception) {
            _voiceState.value = VoiceState.ERROR
            _errorMessage.value = "Speech recognizer initialization error."
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                }

                override fun onError(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                }
            })
        }
    }

    fun startListening(language: AppLanguage = AppLanguage.ENGLISH) {
        mainHandler.post {
            if (speechRecognizer == null) {
                initSpeechRecognizer()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            try {
                speechRecognizer?.startListening(intent)
                _voiceState.value = VoiceState.LISTENING
            } catch (e: Exception) {
                _voiceState.value = VoiceState.ERROR
                _errorMessage.value = e.message ?: "Failed to start speech recognition"
            }
        }
    }

    fun createRecognizerIntent(language: AppLanguage = AppLanguage.ENGLISH): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your health or safety question...")
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {}
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun onSpeechActivityResult(text: String) {
        if (text.isNotBlank()) {
            _voiceState.value = VoiceState.IDLE
            _recognizedText.value = text
        }
    }

    fun speak(text: String, language: AppLanguage = AppLanguage.ENGLISH, force: Boolean = false) {
        if (!isTtsInitialized || textToSpeech == null) return
        if (!force && !isVoiceRepliesEnabled) return

        stopSpeaking()
        val cleanText = text
            .replace(Regex("[*#_~`]"), "")
            .replace(Regex("[📍⌚📌🛡️🌤️📚💡📄🙏👋]"), "")
        textToSpeech?.language = language.locale
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_id_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun setVoiceRepliesEnabled(enabled: Boolean) {
        isVoiceRepliesEnabled = enabled
        if (!enabled) stopSpeaking()
    }

    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                textToSpeech?.stop()
                textToSpeech?.shutdown()
            } catch (e: Exception) {}
        }
    }
}
