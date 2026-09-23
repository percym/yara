package dev.percym.yara.data

data class NearbyShop(
    val id: String,
    val name: String,
    val address: String,
    val category: StoreCategory,
    val distanceMeters: Float
)
