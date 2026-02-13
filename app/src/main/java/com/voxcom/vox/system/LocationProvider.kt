package com.voxcom.vox.system

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

object LocationProvider {

    @SuppressLint("MissingPermission")
    fun getLastLocation(context: Context, onResult: (Location?) -> Unit) {

        val client = LocationServices.getFusedLocationProviderClient(context)

        client.lastLocation
            .addOnSuccessListener { location ->
                onResult(location)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
