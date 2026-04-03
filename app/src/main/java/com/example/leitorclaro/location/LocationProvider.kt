package com.example.leitorclaro.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun fetchAddressInfo(): AddressInfo {
        val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0

        val geocoder = Geocoder(context, Locale("pt", "BR"))
        val address = suspendCancellableCoroutine { cont ->
            geocoder.getFromLocation(lat, lng, 1) { results ->
                cont.resume(results?.firstOrNull())
            }
        }

        return AddressInfo(
            latitude = lat,
            longitude = lng,
            street = address?.thoroughfare.orEmpty(),
            neighborhood = address?.subLocality.orEmpty(),
            postalCode = address?.postalCode.orEmpty()
        )
    }
}
