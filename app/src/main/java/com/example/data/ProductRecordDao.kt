package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductRecordDao {
    @Query("SELECT * FROM product_records ORDER BY registrationDate DESC")
    fun getAllRecords(): Flow<List<ProductRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ProductRecord): Long

    @Update
    suspend fun updateRecord(record: ProductRecord)

    @Delete
    suspend fun deleteRecord(record: ProductRecord)

    @Query("UPDATE product_records SET status = :status, completionDate = :completionDate WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, completionDate: Long?)

    @Query("UPDATE product_records SET status = :status, approvalDate = :approvalDate WHERE id = :id")
    suspend fun approveRecord(id: Int, status: String, approvalDate: Long?)
}
