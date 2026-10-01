package com.jetwatch.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jetwatch.JetWatchApplication
import com.jetwatch.domain.alert.FlightAlertPolicy
import java.io.IOException
import kotlinx.coroutines.delay

class FlightAlertWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val graph = (applicationContext as JetWatchApplication).graph
        val followed = graph.saved.all().filter { it.alertsEnabled }
        if (followed.isEmpty()) return Result.success()
        val notifier = FlightAlertNotifier(applicationContext)
        var networkFailures = 0
        for (flight in followed) {
            try {
                val query = flight.flightNumber ?: flight.callSign.orEmpty()
                val icao24 = if (query.isBlank()) flight.key else null
                val details = graph.flights.lookup(query, icao24, preferNetwork = true) ?: continue
                val kind = FlightAlertPolicy.detect(flight.status, details.status)
                if (kind != null) notifier.notify(flight, kind, details)
                graph.saved.updateFromDetails(flight.key, details)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                networkFailures++
            } catch (_: Exception) {
                // A missing key or a single bad flight should not stop the rest.
            }
            delay(400)
        }
        return if (networkFailures == followed.size) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "flight-alerts"
    }
}
