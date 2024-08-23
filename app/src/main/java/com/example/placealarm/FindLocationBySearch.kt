package com.example.placealarm


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@Composable
fun GetSearchLocation( cameraPositionState: CameraPositionState, searchText: String, onGetLatLng: (LatLng) -> Unit)  {

    val scope = rememberCoroutineScope()

    val context = LocalContext.current


        scope.launch {
            try {

                val placeClient = Places.createClient(context)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchText)
                    .build()
                Log.d("GEOLOCATION", "$request")
                val response = withContext(Dispatchers.IO) {
                    placeClient.findAutocompletePredictions(request).await()
                }

                Log.d("GEOLOCATION", "$response")

                response.autocompletePredictions.firstOrNull()?.let { prediction ->
                    val placeId = prediction.placeId
                    val placeRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()
                    val placeResponse = withContext(Dispatchers.IO) {
                        placeClient.fetchPlace(placeRequest).await()
                    }
                    placeResponse.place.latLng?.let { latlng ->
                        onGetLatLng(latlng)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, cameraPositionState.position.zoom)
//                                cameraPositionState = CameraPositionState(
//                                    position = CameraPosition.fromLatLngZoom(latlng, 15f)
//                                )
                        Log.d("GEOLOCATION", "New latLng: $latlng")
                    }
                }


            } catch (e: CancellationException) {
                Log.d("GEOLOCATION", "e: $e")
            }catch (e: Exception) {
                Log.e("GEOLOCATION", "Error fetching place: $e")
            }

        }

}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->

    addOnCompleteListener {
        if (it.exception != null) {
            continuation.resumeWithException(it.exception!!)
        } else {
            continuation.resume(it.result)
        }
    }

    addOnCanceledListener {
        continuation.cancel()
    }
}