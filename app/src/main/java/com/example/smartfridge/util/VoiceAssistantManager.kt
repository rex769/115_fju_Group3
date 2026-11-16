package com.example.smartfridge.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistantManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.TAIWAN
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FridgeTTS")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
