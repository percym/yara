package dev.percym.yara.data

data class NearbyShop(
    val id: String,
    val name: String,
    val address: String,
    val category: StoreCategory,
    val distanceMeters: Float,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
