package com.kali.service

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.*
import androidx.core.app.NotificationCompat
import com.kali.model.KaliDatabase
import com.kali.model.MediaEntity
import kotlinx.coroutines.*
import java.io.File

class RecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var outFile: File? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var alertLocalId: Int = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alertLocalId = intent?.getIntExtra("alertLocalId", -1) ?: -1
        startForeground(NOTIF_ID, buildNotification())
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        outFile = File(filesDir, "evidence_${System.currentTimeMillis()}.mp4")
        mediaRecorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
        ).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outFile!!.absolutePath)
            prepare()
            start()
        }
    }

    override fun onDestroy() {
        mediaRecorder?.runCatching { stop(); release() }
        mediaRecorder = null
        val savedFile = outFile
        if (alertLocalId != -1 && savedFile != null) {
            scope.launch {
                KaliDatabase.get(applicationContext).mediaDao().insert(
                    MediaEntity(
                        alertLocalId = alertLocalId,
                        filePath = savedFile.absolutePath,
                        mediaType = "audio"
                    )
                )
                SyncWorker.enqueue(applicationContext)
            }
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "kali_recording"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Evidence Recording", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("KALI – Recording Evidence")
            .setContentText("Audio is being captured securely.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    companion object { const val NOTIF_ID = 1002 }
}
