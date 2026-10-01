package com.example.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object AlarmRingtonePlayer {

    private var currentRingtone: Ringtone? = null
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var toneRunnable: Runnable? = null

    val AVAILABLE_RINGTONES = listOf(
        "Default Loud Alarm",
        "Digital Beep",
        "School Bell",
        "Siren Alert",
        "Gentle Chime"
    )

    fun startAlarm(context: Context, ringtoneName: String, vibrate: Boolean = true) {
        stopAlarm()
        isPlaying = true

        // 1. Play chosen ringtone sound
        try {
            when (ringtoneName) {
                "Digital Beep" -> {
                    startDigitalBeep()
                }
                "Siren Alert" -> {
                    startSirenAlert()
                }
                "School Bell" -> {
                    // Try system ringtone for bell-like melody, or fallback to alarm
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    playSystemUri(context, uri)
                }
                "Gentle Chime" -> {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    playSystemUri(context, uri)
                }
                else -> { // "Default Loud Alarm"
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    playSystemUri(context, uri)
                }
            }
        } catch (_: Exception) {
            // Fallback tone generator
            startDigitalBeep()
        }

        // 2. Continuous Vibration even in silent / meeting mode
        if (vibrate) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                // Repeating vibration pattern: 0ms delay, 800ms vibrate, 300ms pause, 800ms vibrate, 300ms pause...
                val pattern = longArrayOf(0, 800, 300, 800, 300, 800, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat indefinitely
                    vibrator?.vibrate(
                        effect,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun playSystemUri(context: Context, uri: Uri) {
        val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = true
        }
        ringtone.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone.play()
        currentRingtone = ringtone

        // For Android versions below P that don't support ringtone.isLooping, schedule replay loop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            toneRunnable = object : Runnable {
                override fun run() {
                    if (isPlaying && currentRingtone?.isPlaying == false) {
                        currentRingtone?.play()
                    }
                    if (isPlaying) {
                        handler.postDelayed(this, 1500)
                    }
                }
            }
            handler.postDelayed(toneRunnable!!, 1500)
        }
    }

    private fun startDigitalBeep() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneRunnable = object : Runnable {
                override fun run() {
                    if (!isPlaying) return
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
                    handler.postDelayed(this, 900)
                }
            }
            handler.post(toneRunnable!!)
        } catch (_: Exception) {
        }
    }

    private fun startSirenAlert() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            var alternate = false
            toneRunnable = object : Runnable {
                override fun run() {
                    if (!isPlaying) return
                    val tone = if (alternate) ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK else ToneGenerator.TONE_CDMA_HIGH_L
                    alternate = !alternate
                    toneGenerator?.startTone(tone, 400)
                    handler.postDelayed(this, 500)
                }
            }
            handler.post(toneRunnable!!)
        } catch (_: Exception) {
        }
    }

    fun stopAlarm() {
        isPlaying = false
        toneRunnable?.let { handler.removeCallbacks(it) }
        toneRunnable = null

        try {
            currentRingtone?.stop()
            currentRingtone = null
        } catch (_: Exception) {
        }

        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (_: Exception) {
        }
    }
}
