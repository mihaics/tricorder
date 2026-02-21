package com.sysop.tricorder.feature.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject
    lateinit var sessionRecorder: SessionRecorder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binder = RecordingBinder()

    inner class RecordingBinder : Binder() {
        fun getRecorder(): SessionRecorder = sessionRecorder
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Recording..."),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
                startElapsedUpdates()
            }
            ACTION_STOP -> {
                serviceScope.launch {
                    sessionRecorder.stopRecording()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startElapsedUpdates() {
        serviceScope.launch {
            sessionRecorder.elapsed.collect { elapsed ->
                val hours = elapsed.toHours()
                val minutes = elapsed.toMinutesPart()
                val seconds = elapsed.toSecondsPart()
                val timeText = if (hours > 0) {
                    "%d:%02d:%02d".format(hours, minutes, seconds)
                } else {
                    "%02d:%02d".format(minutes, seconds)
                }
                updateNotification("Recording: $timeText")
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Session Recording",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows when a sensor recording session is active"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tricorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        const val CHANNEL_ID = "tricorder_recording"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.sysop.tricorder.action.START_RECORDING"
        const val ACTION_STOP = "com.sysop.tricorder.action.STOP_RECORDING"

        fun startIntent(context: Context): Intent =
            Intent(context, RecordingService::class.java).apply {
                action = ACTION_START
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, RecordingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
