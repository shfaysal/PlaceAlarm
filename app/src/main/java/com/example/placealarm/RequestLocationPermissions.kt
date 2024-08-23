package com.example.placealarm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermissions(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionRevoked: () -> Unit,
    context: Context
) {

    // Initialize the state for managing multiple location permission
    val permissionState = rememberMultiplePermissionsState (
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )


    //use launchedEffect to handle permissions logic when the composition is launched
    LaunchedEffect(key1 = permissionState) {
        //check if all previously granted permissions are revoked
        val allPermissionsRevoked = permissionState.permissions.size == permissionState.revokedPermissions.size

        //filter permissions that need to be requested
        val permissionsToRequest = permissionState.permissions.filter {
            !it.status.isGranted
        }

        //if there are permissions to request, launch the permission request
        if (permissionsToRequest.isNotEmpty()) {
            permissionState.launchMultiplePermissionRequest()
        }

        //execute callbacks based on permission status
        if (allPermissionsRevoked) {
            onPermissionRevoked()
        } else {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

    }

}

fun areLocationPermissionsGranted(context: Context): Boolean {
    return (ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
}