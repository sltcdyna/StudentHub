package com.dyna.studenthub.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TaskReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Rappels d'études", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val title = inputData.getString(KEY_TITLE) ?: "Échéance proche"
        val type = inputData.getString(KEY_TYPE) ?: "Devoir"
        NotificationManagerCompat.from(applicationContext).notify(
            inputData.getLong(KEY_ID, System.currentTimeMillis()).toInt(),
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(com.dyna.studenthub.R.drawable.ic_launcher_foreground)
                .setContentTitle("Rappel $type")
                .setContentText("$title est à rendre demain.")
                .setAutoCancel(true)
                .build()
        )
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "study_reminders"
        const val KEY_TITLE = "title"
        const val KEY_TYPE = "type"
        const val KEY_ID = "id"
    }
}
