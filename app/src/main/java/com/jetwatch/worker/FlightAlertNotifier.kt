package com.jetwatch.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jetwatch.JetWatchApplication
import com.jetwatch.MainActivity
import com.jetwatch.R
import com.jetwatch.domain.alert.AlertKind
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.ui.format.routeLabel
import com.jetwatch.ui.format.statusLabel

class FlightAlertNotifier(private val context: Context) {
    fun notify(flight: SavedFlight, kind: AlertKind, details: FlightDetails) {
        val route = routeLabel(
            details.originIata ?: flight.originIata,
            details.destinationIata ?: flight.destinationIata,
        )
        val (title, text) = when (kind) {
            AlertKind.Delayed -> "Delayed" to "$route is delayed"
            AlertKind.Departed -> "Departed" to "$route has departed"
            AlertKind.Landed -> "Landed" to "$route has landed"
        }
        val number = details.flightNumber
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(JetWatchApplication.EXTRA_FLIGHT_KEY, flight.key)
        }
        val pending = PendingIntent.getActivity(
            context,
            flight.key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, JetWatchApplication.ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_plane)
            .setContentTitle("$number · $title")
            .setContentText("$text · ${statusLabel(details.status)}")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(flight.key.hashCode(), notification)
        }
    }
}
