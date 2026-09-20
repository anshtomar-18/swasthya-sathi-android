package com.swasthyasathi.app.sos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SOSWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sosQueue = SOSQueue(applicationContext)
        return try {
            val success = sosQueue.processQueueIfOnline()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
