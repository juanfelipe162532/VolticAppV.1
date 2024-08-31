package com.lhdevelopment.voltic

import android.content.Context
import android.widget.ArrayAdapter
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient

class PlacesAutoCompleteAdapter(
    context: Context,
    private val placesClient: PlacesClient
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {

    private val placeIds = mutableListOf<String>()
    private val predictions = mutableListOf<String>()

    override fun getItem(position: Int): String? {
        return if (position < predictions.size) predictions[position] else null
    }

    override fun getCount(): Int {
        return predictions.size
    }

    fun updatePredictions(predictions: List<AutocompletePrediction>) {
        this.predictions.clear()
        this.placeIds.clear()
        for (prediction in predictions) {
            this.predictions.add(prediction.getPrimaryText(null).toString())
            this.placeIds.add(prediction.placeId)
        }
        notifyDataSetChanged()
    }

    fun getPlaceId(position: Int): String? {
        return if (position < placeIds.size) placeIds[position] else null
    }
}
