package dev.percym.yara.data

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
