package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Sound and Haptic Alert Utility for Emergency Dispatch Notifications.
 * Plays system ringtones or ToneGenerator tones with haptic feedback.
 */
object SoundHelper {

    fun playRegistrationSuccess(context: Context) {
        playSoundAndVibrate(
            context = context,
            toneType = ToneGenerator.TONE_PROP_BEEP2,
            durationMs = 200,
            ringtoneType = RingtoneManager.TYPE_NOTIFICATION,
            isUrgent = false
        )
    }

    fun playCriticalAlert(context: Context) {
        playSoundAndVibrate(
            context = context,
            toneType = ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,
            durationMs = 600,
            ringtoneType = RingtoneManager.TYPE_ALARM,
            isUrgent = true
        )
    }

    fun playNotificationSound(context: Context) {
        playSoundAndVibrate(
            context = context,
            toneType = ToneGenerator.TONE_PROP_ACK,
            durationMs = 250,
            ringtoneType = RingtoneManager.TYPE_NOTIFICATION,
            isUrgent = false
        )
    }

    fun playDispatchChime(context: Context) {
        playSoundAndVibrate(
            context = context,
            toneType = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
            durationMs = 350,
            ringtoneType = RingtoneManager.TYPE_NOTIFICATION,
            isUrgent = false
        )
    }

    private fun playSoundAndVibrate(
        context: Context,
        toneType: Int,
        durationMs: Int,
        ringtoneType: Int,
        isUrgent: Boolean
    ) {
        // 1. Play audio sound safely
        try {
            val soundUri = RingtoneManager.getDefaultUri(ringtoneType)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(if (isUrgent) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
            } else {
                fallbackTone(toneType, durationMs)
            }
        } catch (_: Throwable) {
            fallbackTone(toneType, durationMs)
        }

        // 2. Play tactile haptic vibration safely
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isUrgent) {
                        val timings = longArrayOf(0, 150, 80, 200, 80, 250)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isUrgent) 400L else 120L)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun fallbackTone(toneType: Int, durationMs: Int) {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(toneType, durationMs)
        } catch (_: Throwable) {}
    }
}
