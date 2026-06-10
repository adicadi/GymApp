package com.example.gymapp.wear

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Streams today's running totals for steps and active calories straight from
 * the watch's own sensors via Health Services passive monitoring — the
 * steps/calories analogue of [WatchHeartRateMonitor].
 */
object WatchActivityMonitor {
    private val _steps = MutableStateFlow<Long?>(null)
    val steps: StateFlow<Long?> = _steps.asStateFlow()

    private val _calories = MutableStateFlow<Double?>(null)
    val calories: StateFlow<Double?> = _calories.asStateFlow()

    private var registered = false

    private val callback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            dataPoints.getData(DataType.STEPS_DAILY).lastOrNull()?.let { _steps.value = it.value }
            dataPoints.getData(DataType.CALORIES_DAILY).lastOrNull()?.let { _calories.value = it.value }
        }
    }

    fun start(context: Context) {
        if (registered) return
        registered = true
        HealthServices.getClient(context.applicationContext).passiveMonitoringClient
            .setPassiveListenerCallback(
                PassiveListenerConfig.Builder()
                    .setDataTypes(setOf(DataType.STEPS_DAILY, DataType.CALORIES_DAILY))
                    .build(),
                callback,
            )
    }

    fun stop(context: Context) {
        if (!registered) return
        registered = false
        _steps.value = null
        _calories.value = null
        HealthServices.getClient(context.applicationContext).passiveMonitoringClient
            .clearPassiveListenerCallbackAsync()
    }
}
