package dev.percym.yara.data

enum class StoreCategory(val displayName: String, val placeType: String) {
    GROCERY("Grocery / Supermarket", "grocery_or_supermarket"),
    PHARMACY("Pharmacy", "pharmacy"),
    BAKERY("Bakery", "bakery"),
    HARDWARE("Hardware Store", "hardware_store"),
    CONVENIENCE("Convenience Store", "convenience_store");
}
