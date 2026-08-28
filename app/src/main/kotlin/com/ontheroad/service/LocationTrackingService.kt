package com.ontheroad.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ontheroad.MainActivity
import com.ontheroad.OnTheRoadApplication
import com.ontheroad.R
import com.ontheroad.core.data.repository.TripRepositoryImpl
import com.ontheroad.core.domain.usecase.RecordRoutePointUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service providing uninterrupted high-accuracy GPS tracking
 * during active driver trips, even when backgrounded or locked.
 */
class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recordRoutePointUseCase: RecordRoutePointUseCase
    private var activeTripId: String? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val tripId = activeTripId ?: return
            for (location in result.locations) {
                serviceScope.launch {
                    val updatedTrip = recordRoutePointUseCase(
                        tripId = tripId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        accuracyMeters = location.accuracy,
                        speedMps = location.speed,
                        timestampMillis = location.time
                    )
                    if (updatedTrip != null) {
                        updateNotification(updatedTrip.actualDistanceKm)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val app = application as OnTheRoadApplication
        val tripRepository = TripRepositoryImpl(app.database.tripDao())
        recordRoutePointUseCase = RecordRoutePointUseCase(tripRepository)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val tripId = intent.getStringExtra(EXTRA_TRIP_ID)
                if (tripId != null) {
                    activeTripId = tripId
                    startForegroundServiceWithNotification()
                    startLocationUpdates()
                }
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification(0.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handled if permission is revoked
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun buildNotification(distanceKm: Double): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = if (distanceKm > 0.0) {
            "Tracking: ${String.format("%.2f", distanceKm)} km"
        } else {
            "Recording GPS breadcrumbs..."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OnTheRoad: Active Run")
            .setContentText(distanceText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(distanceKm: Double) {
        val notification = buildNotification(distanceKm)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trip Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status and odometer distance for active runs"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ontheroad.action.START_TRACKING"
        const val ACTION_STOP = "com.ontheroad.action.STOP_TRACKING"
        const val EXTRA_TRIP_ID = "extra_trip_id"
        private const val CHANNEL_ID = "ontheroad_tracking_channel"
        private const val NOTIFICATION_ID = 1001

        fun startTracking(context: Context, tripId: String) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TRIP_ID, tripId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
