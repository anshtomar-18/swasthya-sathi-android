package com.swasthyasathi.app.sos

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SOSManager(private val context: Context) {

    val queue = SOSQueue(context)

    private val _sosState = MutableStateFlow(SOSState.NORMAL)
    val sosState: StateFlow<SOSState> = _sosState.asStateFlow()

    private val _lastQueuedEvent = MutableStateFlow<SOSEvent?>(null)
    val lastQueuedEvent: StateFlow<SOSEvent?> = _lastQueuedEvent.asStateFlow()

    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun setDemoMode(demo: Boolean) {
        _isDemoMode.value = demo
    }

    suspend fun triggerEmergencySOS(event: SOSEvent, isOffline: Boolean) {
        _sosState.value = SOSState.EMERGENCY_DETECTED
        _lastQueuedEvent.value = event

        if (isOffline) {
            _sosState.value = SOSState.SOS_QUEUED
            queue.queueSosEvent(event.copy(queuedOffline = true))
            scheduleBackgroundSync()
        } else {
            _sosState.value = SOSState.SOS_TRANSMITTING
            queue.queueSosEvent(event.copy(queuedOffline = false))
            val success = queue.processQueueIfOnline()
            if (success) {
                _sosState.value = SOSState.SOS_SENT
            } else {
                _sosState.value = SOSState.SOS_QUEUED
                scheduleBackgroundSync()
            }
        }
    }

    fun onConnectivityRestored() {
        if (_sosState.value == SOSState.SOS_QUEUED || _sosState.value == SOSState.WAITING_FOR_CONNECTION) {
            _sosState.value = SOSState.SOS_TRANSMITTING
            scheduleImmediateSync()
        }
    }

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWork = OneTimeWorkRequestBuilder<SOSWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncWork)
    }

    private fun scheduleImmediateSync() {
        val syncWork = OneTimeWorkRequestBuilder<SOSWorker>().build()
        WorkManager.getInstance(context).enqueue(syncWork)
    }

    fun resetState() {
        _sosState.value = SOSState.NORMAL
        _lastQueuedEvent.value = null
    }
}
