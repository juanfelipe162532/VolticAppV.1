package com.lhdevelopment.voltic

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>
) {
    data class Route(
        @SerializedName("overview_polyline") val overviewPolyline: Polyline
    ) {
        data class Polyline(
            @SerializedName("points") val points: String
        )
    }
}
