package dev.percym.yara.ui.products

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dev.percym.yara.data.NearbyShop
import dev.percym.yara.data.Product
import dev.percym.yara.data.ProductRepository
import dev.percym.yara.data.StoreCategory
import dev.percym.yara.geofence.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository()
    private val geofenceManager = GeofenceManager(application)
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _nearbyShops = MutableStateFlow<List<NearbyShop>>(emptyList())
    val nearbyShops: StateFlow<List<NearbyShop>> = _nearbyShops.asStateFlow()

    private val _isLoadingShops = MutableStateFlow(false)
    val isLoadingShops: StateFlow<Boolean> = _isLoadingShops.asStateFlow()

    init {
        viewModelScope.launch {
            var firstLoad = true
            repository.observeProducts().collect { products ->
                _products.value = products
                if (firstLoad && products.isNotEmpty()) {
                    firstLoad = false
                    refreshAllGeofences(products)
                }
            }
        }
    }

    fun addProduct(name: String, category: StoreCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.addProduct(name, category)
                refreshGeofences(category)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }

    @SuppressLint("MissingPermission")
    fun loadNearbyShops() {
        viewModelScope.launch {
            _isLoadingShops.value = true
            _nearbyShops.value = emptyList()
            _error.value = null
            try {
                val cts = CancellationTokenSource()
                val location = fusedLocation.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, cts.token
                ).await()
                if (location == null) {
                    _error.value = "Could not get location — ensure Location permission is granted"
                    return@launch
                }
                _nearbyShops.value = geofenceManager.searchNearbyShops(
                    location.latitude, location.longitude
                )
                if (_nearbyShops.value.isEmpty()) {
                    _error.value = "No shops found — check that Places API (New) is enabled and billing is active in Google Cloud Console"
                } else {
                    // User is here now — register geofences from this location so
                    // they fire when the user comes back later
                    refreshAllGeofences(_products.value, location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _isLoadingShops.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun syncGeofences() {
        viewModelScope.launch {
            refreshAllGeofences(_products.value)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun refreshAllGeofences(products: List<Product>, lat: Double? = null, lng: Double? = null) {
        if (products.isEmpty()) return
        try {
            val resolvedLat: Double
            val resolvedLng: Double
            if (lat != null && lng != null) {
                resolvedLat = lat
                resolvedLng = lng
            } else {
                val cts = CancellationTokenSource()
                val loc = fusedLocation.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                ).await() ?: return
                resolvedLat = loc.latitude
                resolvedLng = loc.longitude
            }

            val categoriesInUse = products
                .mapNotNull { runCatching { StoreCategory.valueOf(it.category) }.getOrNull() }
                .distinct()

            for (category in categoriesInUse) {
                val names = products.filter { it.category == category.name }.map { it.name }
                geofenceManager.refreshForCategory(category, names, resolvedLat, resolvedLng)
            }
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun refreshGeofences(category: StoreCategory) {
        try {
            val cts = CancellationTokenSource()
            val location = fusedLocation.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
            ).await() ?: return

            val productsForCategory = _products.value
                .filter { it.category == category.name }
                .map { it.name }

            geofenceManager.refreshForCategory(
                category, productsForCategory,
                location.latitude, location.longitude
            )
        } catch (_: SecurityException) {
            // No location permission — geofences will be set up once permission is granted
        } catch (_: Exception) {
            // Location unavailable, silently skip
        }
    }
}
