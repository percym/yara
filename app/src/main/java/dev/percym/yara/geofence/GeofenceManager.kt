package dev.percym.yara.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import android.location.Location
import dev.percym.yara.data.NearbyShop
import dev.percym.yara.data.StoreCategory
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class GeofenceManager(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yara_geofences", Context.MODE_PRIVATE)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun refreshForCategory(
        category: StoreCategory,
        products: List<String>,
        userLat: Double,
        userLng: Double
    ) {
        if (!hasFineLocationPermission()) return

        saveProductMapping(category, products)

        val placesClient = Places.createClient(context)
        val request = SearchNearbyRequest.builder(
            CircularBounds.newInstance(LatLng(userLat, userLng), 5000.0),
            listOf(Place.Field.ID, Place.Field.LOCATION)
        )
            .setIncludedTypes(listOf(category.placeType))
            .build()

        try {
            val response = placesClient.searchNearby(request).await()
            val geofences = response.places.mapNotNull { place ->
                val latlng = place.location ?: return@mapNotNull null
                Geofence.Builder()
                    .setRequestId("${category.name}__${place.id}")
                    .setCircularRegion(latlng.latitude, latlng.longitude, 150f)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            }

            if (geofences.isNotEmpty()) {
                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(geofences)
                    .build()
                geofencingClient.addGeofences(geofencingRequest, pendingIntent).await()
                Log.d("GeofenceManager", "Registered ${geofences.size} geofences for ${category.name}")
            }
        } catch (e: Exception) {
            Log.e("GeofenceManager", "Failed to set geofences for ${category.name}", e)
        }
    }

    suspend fun searchNearbyShops(
        userLat: Double,
        userLng: Double,
        categories: List<StoreCategory> = StoreCategory.entries
    ): List<NearbyShop> {
        val placesClient = Places.createClient(context)
        val results = mutableListOf<NearbyShop>()
        val distOut = FloatArray(1)

        for (category in categories) {
            try {
                val request = SearchNearbyRequest.builder(
                    CircularBounds.newInstance(LatLng(userLat, userLng), 3000.0),
                    listOf(Place.Field.ID, Place.Field.LOCATION, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)
                )
                    .setIncludedTypes(listOf(category.placeType))
                    .setMaxResultCount(5)
                    .build()

                val response = placesClient.searchNearby(request).await()
                response.places.mapNotNullTo(results) { place ->
                    val latlng = place.location ?: return@mapNotNullTo null
                    val placeId = place.id ?: return@mapNotNullTo null
                    Location.distanceBetween(userLat, userLng, latlng.latitude, latlng.longitude, distOut)
                    NearbyShop(
                        id = placeId,
                        name = place.displayName ?: "Unknown",
                        address = place.formattedAddress ?: "",
                        category = category,
                        distanceMeters = distOut[0]
                    )
                }
            } catch (e: Exception) {
                Log.e("GeofenceManager", "Nearby search failed for ${category.name}", e)
            }
        }

        return results.sortedBy { it.distanceMeters }
    }

    suspend fun refreshGeofencesFromCache(userLat: Double, userLng: Double) {
        val mapping = readProductMapping()
        for ((categoryName, products) in mapping) {
            if (products.isEmpty()) continue
            val category = runCatching { StoreCategory.valueOf(categoryName) }.getOrNull() ?: continue
            refreshForCategory(category, products, userLat, userLng)
        }
    }

    fun getProductsForGeofenceId(geofenceId: String): List<String> {
        val categoryName = geofenceId.substringBefore("__")
        return readProductMapping()[categoryName] ?: emptyList()
    }

    private fun saveProductMapping(category: StoreCategory, products: List<String>) {
        val map = readProductMapping().toMutableMap()
        map[category.name] = products
        val json = JSONObject().apply {
            map.forEach { (k, v) -> put(k, JSONArray(v)) }
        }
        prefs.edit().putString("product_map", json.toString()).apply()
    }

    private fun readProductMapping(): Map<String, List<String>> {
        val raw = prefs.getString("product_map", "{}") ?: "{}"
        return try {
            val obj = JSONObject(raw)
            obj.keys().asSequence().associateWith { key ->
                val arr = obj.getJSONArray(key)
                (0 until arr.length()).map { arr.getString(it) }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun hasFineLocationPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
