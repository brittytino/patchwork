package com.brittytino.essentials.domain.model

data class LocationAlarm(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Int = 1000, // in meters
    val isEnabled: Boolean = false
)
