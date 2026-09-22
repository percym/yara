package dev.percym.yara.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun collection() =
        firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("products")

    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val listener = collection().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.toObjects<Product>() ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(name: String, category: StoreCategory) {
        val product = Product(
            name = name.trim(),
            category = category.name,
            userId = auth.currentUser!!.uid
        )
        collection().add(product).await()
    }

    suspend fun deleteProduct(productId: String) {
        collection().document(productId).delete().await()
    }
}
