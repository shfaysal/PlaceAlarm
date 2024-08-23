package com.example.placealarm

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.placealarm.ui.theme.PlaceAlarmTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private lateinit var fusedLocationClientForCurrentPlace: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequired : Boolean = false

    override fun onResume() {
        super.onResume()
        if (locationRequired) {
            startLocationUpdates( locationCallback!!, fusedLocationClientForCurrentPlace,)
        }
    }




    @SuppressLint("UnrememberedMutableState")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            PlaceAlarmTheme {

                fusedLocationClientForCurrentPlace = LocationServices.getFusedLocationProviderClient(this)

                var currentLocation by remember {
                    mutableStateOf(LatLng(0.toDouble(), 0.toDouble()))
                }

                val cameraPosition = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        currentLocation, 10f
                    )
                }

                var cameraPositionState by remember {
                    mutableStateOf(cameraPosition)
                }



                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        super.onLocationResult(p0)
                        for (location in p0.locations) {
                            currentLocation = LatLng(location.latitude, location.longitude)

//                            Log.d("GEOLOCATION", "LocationCallback ${Math.round(lastLocation.latitude * scale) / scale.toDouble()} $locationBtnClicked $currentLocation last = $lastLocation")
//                                locationBtnClicked = false
//                                lastLocation = currentLocation
                            cameraPositionState = CameraPositionState(
                                position = CameraPosition.fromLatLngZoom(
                                    currentLocation, cameraPositionState.position.zoom
                                )
                            )

                        }
                    }
                }



                Log.d("NEWLNG", "Zoom Level = ${cameraPositionState.position.zoom}")

                CurrentLocationScreen(
                    context = this,
                    currentLocation = currentLocation,
                    cameraPositionState = cameraPositionState,
                    locationCallback = locationCallback!!,
                    fusedLocationClientForCurrentPlace = fusedLocationClientForCurrentPlace
                )

            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationClientForCurrentPlace.removeLocationUpdates(it)
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PlaceAlarmTheme {
        Greeting("Android")
    }
}