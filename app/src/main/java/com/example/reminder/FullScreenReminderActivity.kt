package com.example.reminder

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SettingsRepository
import com.example.ui.theme.StudyFlowTheme

class FullScreenReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Task Reminder"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val venue = intent.getStringExtra(EXTRA_VENUE) ?: ""
        val isMorning = intent.getBooleanExtra(EXTRA_IS_MORNING, false)
        val isExam = intent.getBooleanExtra(EXTRA_IS_EXAM, false)
        val isSessionStarted = intent.getBooleanExtra(EXTRA_IS_SESSION_STARTED, false)

        val settingsRepo = SettingsRepository(applicationContext)
        val ringtone = settingsRepo.settings.value.selectedRingtone
        val vibrate = settingsRepo.settings.value.vibrateInSilentMode

        // Start loud alarm and vibration
        AlarmRingtonePlayer.startAlarm(this, ringtone, vibrate)

        setContent {
            StudyFlowTheme {
                FullScreenReminderContent(
                    title = title,
                    time = time,
                    venue = venue,
                    isMorning = isMorning,
                    isExam = isExam,
                    isSessionStarted = isSessionStarted,
                    onDismiss = {
                        AlarmRingtonePlayer.stopAlarm()
                        finish()
                    },
                    onSnooze = {
                        AlarmRingtonePlayer.stopAlarm()
                        val mgr = ReminderManager(applicationContext)
                        mgr.scheduleAlert(
                            System.currentTimeMillis() + 5 * 60 * 1000,
                            title = "Snoozed: $title",
                            time = time,
                            venue = venue,
                            notificationId = (System.currentTimeMillis() % 10000).toInt(),
                            isMorningBriefing = isMorning
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmRingtonePlayer.stopAlarm()
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_VENUE = "extra_venue"
        const val EXTRA_IS_MORNING = "extra_is_morning"
        const val EXTRA_IS_EXAM = "extra_is_exam"
        const val EXTRA_IS_SESSION_STARTED = "extra_is_session_started"
    }
}

@Composable
fun FullScreenReminderContent(
    title: String,
    time: String,
    venue: String,
    isMorning: Boolean,
    isExam: Boolean,
    isSessionStarted: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundBrush = when {
        isExam -> Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF1E1B4B), Color(0xFF0F172A)))
        isSessionStarted -> Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF0D9488), Color(0xFF0F172A)))
        isMorning -> Brush.verticalGradient(listOf(Color(0xFFD97706), Color(0xFF1E3A8A), Color(0xFF0F172A)))
        else -> Brush.verticalGradient(listOf(Color(0xFF1E40AF), Color(0xFF0F172A), Color(0xFF020617)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Pulse Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isExam -> Icons.Default.School
                        isSessionStarted -> Icons.Default.NotificationsActive
                        isMorning -> Icons.Default.WbSunny
                        else -> Icons.Default.Alarm
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Tag
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when {
                        isExam -> "⚠️ PRIORITY 1: UPCOMING EXAM"
                        isSessionStarted -> "🟢 SESSION STARTING NOW / STARTED!"
                        isMorning -> "⏰ WAKE UP & GET READY"
                        else -> "🔔 UPCOMING SESSION"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message as requested
            val promptMessage = when {
                isExam -> "You have an exam scheduled. Time to review and study!"
                isSessionStarted -> "You have $title at $time.\nSession is Starting NOW!"
                isMorning -> "You have $title at $time.\nWake up and get ready for your morning class!"
                else -> "You have $title at $time.\nPrepare!"
            }

            Text(
                text = promptMessage,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            if (venue.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Venue: $venue", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Dismiss Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .testTag("dismiss_alarm_btn")
            ) {
                Icon(Icons.Default.AlarmOff, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dismiss Alarm", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Snooze Button
            OutlinedButton(
                onClick = onSnooze,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .testTag("snooze_alarm_btn")
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Snooze (5 Mins)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
