package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_records")
data class ProductRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val supplierName: String,
    val quantityNeeded: Int,
    val status: String, // "SOLICITACAO", "APROVACAO", "EFETIVADO"
    val registrationDate: Long = System.currentTimeMillis(),
    val approvalDate: Long? = null,
    val completionDate: Long? = null,
    val expirationDate: Long? = null, // timestamp of product expiration (optional)
    val notes: String = ""
)
