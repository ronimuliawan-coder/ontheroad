package com.ontheroad.core.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.model.AddressSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Android implementation of [LocationRepository] backed by Android Geocoder and FusedLocationProviderClient.
 *
 * Runs exclusively on Dispatchers.IO with graceful error handling and bounding fallback.
 */
class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): AddressSuggestion? = withContext(Dispatchers.IO) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return@withContext null
        }

        try {
            val cts = CancellationTokenSource()
            val location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            if (continuation.isActive) continuation.resume(loc)
                        } else {
                            // Fallback to last known location
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (continuation.isActive) continuation.resume(lastLoc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        }
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                val addressText = reverseGeocode(location.latitude, location.longitude)
                    ?: "Current GPS Location"
                val title = addressText.split(",").firstOrNull()?.trim() ?: "Current Location"

                AddressSuggestion(
                    title = title,
                    fullAddress = addressText,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchAddresses(
        query: String,
        biasLatitude: Double?,
        biasLongitude: Double?,
        maxResults: Int
    ): List<AddressSuggestion> = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext emptyList()

        try {
            var rawAddresses = fetchAddresses(query, biasLatitude, biasLongitude, maxResults)
            if (rawAddresses.isEmpty() && biasLatitude != null && biasLongitude != null) {
                // If local bounding box returned nothing, search broadly without bounding restriction
                rawAddresses = fetchAddresses(query, null, null, maxResults)
            }

            rawAddresses.map { address ->
                val lines = (0..address.maxAddressLineIndex).mapNotNull { address.getAddressLine(it) }
                val fullAddress = if (lines.isNotEmpty()) lines.joinToString(", ") else query
                val title = address.featureName
                    ?: address.thoroughfare
                    ?: address.subLocality
                    ?: address.locality
                    ?: fullAddress.split(",").firstOrNull()?.trim()
                    ?: query

                AddressSuggestion(
                    title = title,
                    fullAddress = fullAddress,
                    latitude = address.latitude,
                    longitude = address.longitude
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchAddresses(
        query: String,
        biasLatitude: Double?,
        biasLongitude: Double?,
        maxResults: Int
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val listener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) continuation.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) continuation.resume(emptyList())
                    }
                }

                if (biasLatitude != null && biasLongitude != null) {
                    val latDelta = 1.0
                    val lngDelta = 1.0
                    geocoder.getFromLocationName(
                        query,
                        maxResults,
                        biasLatitude - latDelta,
                        biasLongitude - lngDelta,
                        biasLatitude + latDelta,
                        biasLongitude + lngDelta,
                        listener
                    )
                } else {
                    geocoder.getFromLocationName(query, maxResults, listener)
                }
            } else {
                @Suppress("DEPRECATION")
                val results = if (biasLatitude != null && biasLongitude != null) {
                    val latDelta = 1.0
                    val lngDelta = 1.0
                    geocoder.getFromLocationName(
                        query,
                        maxResults,
                        biasLatitude - latDelta,
                        biasLongitude - lngDelta,
                        biasLatitude + latDelta,
                        biasLongitude + lngDelta
                    ) ?: emptyList()
                } else {
                    geocoder.getFromLocationName(query, maxResults) ?: emptyList()
                }
                if (continuation.isActive) continuation.resume(results)
            }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(emptyList())
        }
    }

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null

        try {
            val addresses: List<Address> = suspendCancellableCoroutine { continuation ->
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val listener = object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) continuation.resume(addresses)
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(emptyList())
                            }
                        }
                        geocoder.getFromLocation(latitude, longitude, 1, listener)
                    } else {
                        @Suppress("DEPRECATION")
                        val res = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
                        if (continuation.isActive) continuation.resume(res)
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(emptyList())
                }
            }

            val address = addresses.firstOrNull() ?: return@withContext null
            val lines = (0..address.maxAddressLineIndex).mapNotNull { address.getAddressLine(it) }
            if (lines.isNotEmpty()) lines.joinToString(", ") else null
        } catch (e: Exception) {
            null
        }
    }
}
