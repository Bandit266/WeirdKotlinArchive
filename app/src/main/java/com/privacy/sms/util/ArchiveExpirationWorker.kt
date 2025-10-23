package com.privacy.sms.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.privacy.sms.repository.SmsRepository

class ArchiveExpirationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val repository = SmsRepository(applicationContext)
            repository.checkExpiredArchives()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
