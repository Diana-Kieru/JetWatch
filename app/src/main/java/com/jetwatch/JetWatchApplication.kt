package com.jetwatch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jetwatch.worker.FlightAlertWorker
import java.io.File
import java.util.concurrent.TimeUnit
import org.maplibre.android.MapLibre

class JetWatchApplication : Application() {
    lateinit var graph: JetWatchGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = JetWatchGraph(
            config = JetWatchConfig(
                aeroDataBoxKey = BuildConfig.AERODATABOX_API_KEY,
                openSkyClientId = BuildConfig.OPENSKY_CLIENT_ID,
                openSkyClientSecret = BuildConfig.OPENSKY_CLIENT_SECRET,
            ),
            storeFile = File(filesDir, "jetwatch.json").absolutePath,
        )
        MapLibre.getInstance(this)
        createAlertChannel()
        val request = PeriodicWorkRequestBuilder<FlightAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            FlightAlertWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun createAlertChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            ALERT_CHANNEL_ID,
            getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.alert_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ALERT_CHANNEL_ID = "flight_alerts"
        const val EXTRA_FLIGHT_KEY = "flight_key"
    }
}
