package com.example.hello.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val city: String?,
    val district: String?,
    val province: String?
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder by lazy { Geocoder(context, Locale.CHINA) }

    @SuppressLint("MissingPermission")
    fun getLocationFlow(): Flow<LocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setMaxUpdates(1)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = getLocationInfo(location.latitude, location.longitude)
                    trySend(locationData)
                    close()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): LocationData? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val locationData = getLocationInfo(location.latitude, location.longitude)
                        continuation.resume(locationData)
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    @Suppress("DEPRECATION")
    private fun getLocationInfo(latitude: Double, longitude: Double): LocationData {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                LocationData(
                    latitude = latitude,
                    longitude = longitude,
                    city = address.locality ?: address.subAdminArea,
                    district = address.subLocality ?: address.featureName,
                    province = address.adminArea
                )
            } else {
                LocationData(latitude, longitude, null, null, null)
            }
        } catch (e: Exception) {
            LocationData(latitude, longitude, null, null, null)
        }
    }

    @Suppress("DEPRECATION")
    fun searchCities(query: String): List<City> {
        return try {
            val addresses = geocoder.getFromLocationName(query, 10)
            addresses?.mapNotNull { address ->
                if (address.locality != null) {
                    City(
                        name = address.locality,
                        latitude = address.latitude,
                        longitude = address.longitude
                    )
                } else null
            }?.distinctBy { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}