package com.example.placealarm

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun CurrentLocationScreen(
    context: Context,
    currentLocation: LatLng,
    cameraPositionState: CameraPositionState,
    locationCallback: LocationCallback,
    fusedLocationClientForCurrentPlace: FusedLocationProviderClient
) : Boolean {

    val apiKey = BuildConfig.MAPS_API_KEY

    Places.initialize(context, apiKey)

//    val googleApiKey = BuildConfig.

    var locationRequired : Boolean = false
    val scope = rememberCoroutineScope()

    var latLng by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }


//    var camerapositionForsearch = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(latLng, 15f)
//    }
//
//    var cameraPositionStateForSearch by remember {
//        mutableStateOf(camerapositionForsearch)
//    }

    var searchText by remember {
        mutableStateOf(TextFieldValue(""))
    }

    var searchLatLng by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }

    var distance by remember {
        mutableDoubleStateOf(1000.0)
    }

    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    var boundSearchLatLng by remember {
        mutableStateOf(LatLng(searchLatLng.latitude - .5, searchLatLng.longitude - .5))
    }

    var boundCurrentLatLng by remember {
        mutableStateOf(LatLng(currentLocation.latitude + .5, currentLocation.longitude + .5))
    }

    if (searchLatLng != LatLng(0.0, 0.0)) {
        distance = SphericalUtil.computeDistanceBetween(currentLocation, searchLatLng)
    }


    var latLngBounds by remember {
        mutableStateOf(
            LatLngBounds.builder()
                .include(boundCurrentLatLng)
                .include(boundSearchLatLng)
                .build()
        )
//        Log.d("NEWLNG", "Inside latng bounds")
    }

//    Log.d("NEWLNG", "Google api key = $google")


    var btnClicked by remember {
        mutableStateOf(false)
    }

    var uiSettings = remember {
        MapUiSettings(

        )
    }

    var mapProperties = remember {
        MapProperties(
//            latLngBoundsForCameraTarget = LatLngBounds(boundCurrentLatLng, boundSearchLatLng)
            isMyLocationEnabled = true,
        )
    }



    val launchMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() ) { permissions ->
        val areGranted = permissions.values.reduce { acc, next -> acc && next}
        if (areGranted) {
            locationRequired = true
            startLocationUpdates(locationCallback, fusedLocationClientForCurrentPlace)
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            locationRequired = false
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(40.dp)) {
        GoogleMap (
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onPOIClick = { poi ->
                Log.d("NEWLNG", "POI = ${poi.latLng}")
            }
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
            )

            if (searchLatLng != LatLng(0.0, 0.0)) {
                Marker(
                    state = MarkerState(position = searchLatLng),
                )

                Polyline(
                    points = listOf(currentLocation, searchLatLng),
                    color = Color.Black
                )

                Circle(
                    center = searchLatLng,
                    radius = 200.0,
                    fillColor = Color.Yellow
                )
            }


        }

        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it},
                label = { Text(text = "Search place")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if(distance <= 500.0) {
                Log.d("NEWLNG","Place Has come")
                Toast.makeText(context,"You Have reached your destination", Toast.LENGTH_SHORT).show()
            }

            Button(onClick = {
                btnClicked = true
                scope.launch {
                    val placeClient = Places.createClient(context)
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(searchText.text)
                        .build()
                    val response = withContext(Dispatchers.IO) {
                        placeClient.findAutocompletePredictions(request).await()
                    }

                    response.autocompletePredictions.firstOrNull()?.let { prediction ->
                        val placeId = prediction.placeId
                        val placeRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()
                        val placeResponse = withContext(Dispatchers.IO) {
                            placeClient.fetchPlace(placeRequest).await()
                        }
                        placeResponse.place.latLng?.let {latlng ->
                            searchLatLng = latlng
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, cameraPositionState.position.zoom)
//                                cameraPositionState = CameraPositionState(
//                                    position = CameraPosition.fromLatLngZoom(latlng, 15f)
//                                )
                            Log.d("NEWLNG", "New latLng inside button: $searchLatLng")
                        }
                    }
                }
            }) {
                Text(text = "click")
            }

            LaunchedEffect(key1 = Unit) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(latLngBounds, 100)
                )
                Log.d("NEWLNG","INSIDE BOUND")
            }

            if (btnClicked) {
                btnClicked = false
//                 GetSearchLocation(cameraPositionStateForSearch, searchText.toString()) {
//                     searchLatLng = it
//                 }



//                SearchLocationAsync(
//                    context = context,
//                    onGetLatLng = {
//                        searchLatLng = LatLng(it[0].latitude, it[0].longitude)
//                    },
//                    searchText = searchText.toString()
//                )



//                GeoCoderFunc(context = context,
//                    cameraPositionState = cameraPositionState,
//                    searchText = searchText.toString()) {
//                    searchLatLng = it
//                }
                Log.d("GEOLOCATION", "SEARCH = $searchLatLng")
            }

            if (permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            //get Location
                startLocationUpdates(locationCallback, fusedLocationClientForCurrentPlace)
            } else {
                launchMultiplePermissions.launch(permissions)
            }

        }
    }
    
    return locationRequired
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SearchLocationAsync(
    context: Context,
    onGetLatLng: (List<LatLng>) -> Unit,
    searchText: String
) {
    LaunchedEffect(key1 = searchText) {
        try {
            val geocoder = Geocoder(context)
            geocoder.getFromLocationName(
                searchText,
                1,
                @SuppressLint("NewApi")
                object : GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val latlnglist = addresses.map { address ->
                            LatLng(address.latitude, address.longitude)
                        }

                        onGetLatLng(latlnglist)
                    }

                    override fun onError(errorMessage: String?) {
                        Log.d("GEOLOCATION","$errorMessage")
                    }
                }
            )
        } catch ( e: Exception ) {
            Log.d("TAG", "$e")
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun GeoCoderFunc(context: Context, cameraPositionState: CameraPositionState, searchText: String, onGetLatLng: (LatLng) -> Unit) {

    val geoCoder = Geocoder(context)
    val addresses = geoCoder.getFromLocationName(searchText, 1)
    if (addresses!!.isNotEmpty()) {
        val location = addresses[0]
        val searchLocation = LatLng(location.latitude, location.longitude)
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(searchLocation!!, 10f)
        )
        onGetLatLng(searchLocation)
        Log.d("GEOLOCATION", "inside Func")
    }
}


@SuppressLint("MissingPermission")
fun startLocationUpdates(locationCallback:LocationCallback, fusedLocationClientForCurrentPlace: FusedLocationProviderClient) {
    locationCallback?.let {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 100
        )
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(100)
            .build()

        fusedLocationClientForCurrentPlace?.requestLocationUpdates(
            locationRequest,
            it,
            Looper.getMainLooper()
        )
    }
}
