package com.hindivideo.translator.pipeline

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class AndroidTtsEngine(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady: Boolean = false
    private val hindiLocale = Locale("hi", "IN")

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(hindiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("HindiTts", "Hindi voice data missing. User should install Hindi TTS from Android settings.")
                isReady = false
                onInitComplete(false)
            } else {
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("HindiTts", "Started speaking: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("HindiTts", "Finished speaking: $utteranceId")
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("HindiTts", "Error speaking: $utteranceId")
                    }
                })
                isReady = true
                onInitComplete(true)
            }
        } else {
            isReady = false
            onInitComplete(false)
        }
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (isReady && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    /**
     * Intent to open Android Settings for downloading Hindi TTS voice data
     */
    fun getInstallTtsDataIntent(): Intent {
        return Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
    }
}