package com.lhdevelopment.voltic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainPanelViewModel : ViewModel() {
    private val _distance = MutableLiveData<Float>()
    val distance: LiveData<Float> get() = _distance

    private val _speed = MutableLiveData<Float>()
    val speed: LiveData<Float> get() = _speed

    private var totalDistance: Float = 0f

    fun updateDistance(newDistance: Float) {
        totalDistance += newDistance
        _distance.value = totalDistance / 1000 // en km
    }

    fun updateSpeed(newSpeed: Float) {
        _speed.value = newSpeed
    }
}
