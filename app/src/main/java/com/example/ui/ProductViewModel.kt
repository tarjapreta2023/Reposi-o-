package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProductRecord
import com.example.data.ProductRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    val allRecords: StateFlow<List<ProductRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter

    // Filtered data based on search queries and status filters
    val filteredRecords: StateFlow<List<ProductRecord>> = combine(
        allRecords,
        searchQuery,
        statusFilter
    ) { records, query, status ->
        records.filter { record ->
            val matchesQuery = query.isEmpty() ||
                    record.productName.contains(query, ignoreCase = true) ||
                    record.supplierName.contains(query, ignoreCase = true) ||
                    record.notes.contains(query, ignoreCase = true)

            val matchesStatus = status == null || record.status == status

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun addRecord(
        name: String,
        supplier: String,
        quantity: Int,
        notes: String,
        expirationDate: Long?,
        initialStatus: String = "SOLICITACAO"
    ) {
        viewModelScope.launch {
            val record = ProductRecord(
                productName = name,
                supplierName = supplier,
                quantityNeeded = quantity,
                status = initialStatus,
                notes = notes,
                expirationDate = expirationDate,
                registrationDate = System.currentTimeMillis()
            )
            repository.insert(record)
        }
    }

    fun approveRecord(recordId: Int) {
        viewModelScope.launch {
            repository.approveRecord(recordId, "APROVACAO", System.currentTimeMillis())
        }
    }

    fun completeRecord(recordId: Int) {
        viewModelScope.launch {
            repository.updateStatus(recordId, "EFETIVADO", System.currentTimeMillis())
        }
    }

    fun deleteRecord(record: ProductRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }

    // Helper to identify products close to expiration (e.g. within 30 days)
    fun getNearExpirationRecords(records: List<ProductRecord>, daysThreshold: Int = 30): List<ProductRecord> {
        val now = System.currentTimeMillis()
        val limit = now + (daysThreshold.toLong() * 24 * 60 * 60 * 1000)
        return records.filter { record ->
            record.expirationDate != null && record.expirationDate >= (now - 24 * 60 * 60 * 1000) && record.expirationDate <= limit
        }.sortedBy { it.expirationDate }
    }

    // Helper to format timestamps to readable dates
    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "-"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Export selected or all records to CSV and share via Intent
    fun shareCsvViaWhatsApp(context: Context, recordsToExport: List<ProductRecord>) {
        val csvHeader = "ID,Produto,Fornecedor,Quantidade,Status,Data Registro,Data Vencimento,Observacoes\n"
        val csvBody = recordsToExport.joinToString("\n") { record ->
            val regDate = formatDate(record.registrationDate)
            val expDate = formatDate(record.expirationDate)
            val sanitizedName = record.productName.replace(",", " ")
            val sanitizedSupplier = record.supplierName.replace(",", " ")
            val sanitizedNotes = record.notes.replace(",", " ").replace("\n", " ")
            "${record.id},$sanitizedName,$sanitizedSupplier,${record.quantityNeeded},${record.status},$regDate,$expDate,$sanitizedNotes"
        }

        val shareContent = """
        📊 *Relatório de Faltas e Reposições*
        Gerado em: ${formatDate(System.currentTimeMillis())}
        Total de itens: ${recordsToExport.size}
        
        ```
        $csvHeader$csvBody
        ```
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContent)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Suggest WhatsApp package if possible but do standard chooser so it works everywhere
            `package` = "com.whatsapp"
        }

        try {
            context.startActivity(sendIntent)
        } catch (ex: Exception) {
            // Fallback to chooser if WhatsApp is not installed directly or package constraint fails
            val chooserIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareContent)
                type = "text/plain"
            }, "Exportar Relatório").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
