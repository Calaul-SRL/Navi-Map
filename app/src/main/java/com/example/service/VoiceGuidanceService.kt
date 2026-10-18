package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceGuidanceService(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    var isMuted = false
    private var lastSpokenText: String = ""
    private var lastSpokenTime: Long = 0L

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isReady = true
        }
    }

    fun speak(text: String, isPriority: Boolean = false) {
        if (isMuted || !isReady || text.isBlank()) return

        val now = System.currentTimeMillis()
        if (!isPriority && text == lastSpokenText && (now - lastSpokenTime) < 15000L) {
            return // Skip repeated announcements within 15 seconds
        }

        lastSpokenText = text
        lastSpokenTime = now

        val queueMode = if (isPriority) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "AutoNav_${now}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
