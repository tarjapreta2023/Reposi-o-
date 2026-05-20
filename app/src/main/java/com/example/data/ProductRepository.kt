package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductRecordDao) {
    val allRecords: Flow<List<ProductRecord>> = dao.getAllRecords()

    suspend fun insert(record: ProductRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun update(record: ProductRecord) {
        dao.updateRecord(record)
    }

    suspend fun delete(record: ProductRecord) {
        dao.deleteRecord(record)
    }

    suspend fun updateStatus(id: Int, status: String, completionDate: Long?) {
        dao.updateStatus(id, status, completionDate)
    }

    suspend fun approveRecord(id: Int, status: String, approvalDate: Long?) {
        dao.approveRecord(id, status, approvalDate)
    }
}
