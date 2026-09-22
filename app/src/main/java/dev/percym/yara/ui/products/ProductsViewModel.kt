package dev.percym.yara.ui.products

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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

    init {
        viewModelScope.launch {
            repository.observeProducts().collect { _products.value = it }
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
