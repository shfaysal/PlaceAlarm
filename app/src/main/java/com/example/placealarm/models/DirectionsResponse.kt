package com.example.placealarm.models

import com.google.android.gms.maps.model.Polyline

data class DirectionsResponse(
    val routes : List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points : String
)
