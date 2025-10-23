package com.privacy.sms

import android.app.Application
import androidx.work.*
import com.privacy.sms.util.ArchiveExpirationWorker
import java.util.concurrent.TimeUnit

class PrivacySMSApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic check for expired archives
        scheduleArchiveExpirationCheck()
    }
    
    private fun scheduleArchiveExpirationCheck() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<ArchiveExpirationWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "archive_expiration_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
