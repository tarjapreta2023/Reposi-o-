package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ProductViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }
    
    // Observers
    val allRecords by viewModel.allRecords.collectAsState()
    val filteredRecords by viewModel.filteredRecords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    
    // Expiration alerts computations
    val nearExpirationRecords = viewModel.getNearExpirationRecords(allRecords)
    val criticalCount = nearExpirationRecords.count { record ->
        val remaining = record.expirationDate?.minus(System.currentTimeMillis()) ?: 0L
        remaining <= 7L * 24 * 60 * 60 * 1000 // Expired or <= 7 days
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "Falta & Reposição", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Gestão de Reposição",
                            fontSize = 11.sp,
                            color = BentoTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Alert Icon badge matching the HTML: a w-10 h-10 rounded-full bg-[#D3E4FF] flex items-center justify-center
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BentoSolicitadosBg, CircleShape)
                                .clickable { currentTab = 3 }
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alertas",
                                tint = BentoSolicitadosText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (nearExpirationRecords.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .background(BentoAlertAccent, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nearExpirationRecords.size.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val accentColor = BentoSolicitadosBg
                val selectedTextColor = BentoSolicitadosText
                val idleTextColor = BentoTextSecondary

                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_panel"),
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedTextColor,
                        unselectedIconColor = idleTextColor,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = idleTextColor,
                        indicatorColor = accentColor
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Novo Registro") },
                    label = { Text("Registrar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_add"),
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedTextColor,
                        unselectedIconColor = idleTextColor,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = idleTextColor,
                        indicatorColor = accentColor
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Log Histórico") },
                    label = { Text("Histórico", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_history"),
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedTextColor,
                        unselectedIconColor = idleTextColor,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = idleTextColor,
                        indicatorColor = accentColor
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { 
                        Box {
                            Icon(Icons.Default.Warning, contentDescription = "Alertas")
                            if (nearExpirationRecords.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .background(BentoAlertAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = nearExpirationRecords.size.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text("Alertas", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_alerts"),
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedTextColor,
                        unselectedIconColor = idleTextColor,
                        selectedTextColor = selectedTextColor,
                        unselectedTextColor = idleTextColor,
                        indicatorColor = accentColor
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BentoBackground)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    allRecords = allRecords,
                    nearExpirationCount = nearExpirationRecords.size,
                    criticalCount = criticalCount,
                    onNavigateToTab = { currentTab = it },
                    onShareCsv = { viewModel.shareCsvViaWhatsApp(context, allRecords) }
                )
                1 -> FormScreen(
                    onAddSuccess = { 
                        currentTab = 2 // Go to list to see the changes
                    },
                    viewModel = viewModel
                )
                2 -> HistoryScreen(
                    records = filteredRecords,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                    onApprove = { viewModel.approveRecord(it) },
                    onComplete = { viewModel.completeRecord(it) },
                    onDelete = { viewModel.deleteRecord(it) },
                    onShareCsv = { viewModel.shareCsvViaWhatsApp(context, filteredRecords) },
                    viewModel = viewModel
                )
                3 -> AlertsScreen(
                    nearExpirationRecords = nearExpirationRecords,
                    viewModel = viewModel
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 0: DASHBOARD (Bento Grid Theme)
// -----------------------------------------------------------------
@Composable
fun DashboardScreen(
    allRecords: List<ProductRecord>,
    nearExpirationCount: Int,
    criticalCount: Int,
    onNavigateToTab: (Int) -> Unit,
    onShareCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inventário",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = BentoTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Gestão de Reposição",
                    fontSize = 13.sp,
                    color = BentoTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Row 3: Expiry Alerta (Full width) - matches [#FFDAD6]
        if (nearExpirationCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(3) }, // Navigate to alerts tab
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoAlertBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BentoAlertAccent, RoundedCornerShape(14.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alerta",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Alerta de Vencimento",
                                fontWeight = FontWeight.Bold,
                                color = BentoAlertText,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (criticalCount > 0)
                                    "Existem $nearExpirationCount produtos próximos do vencimento ($criticalCount críticos)."
                                else
                                    "Você tem $nearExpirationCount produtos próximos do vencimento.",
                                color = Color(0xFF93000A),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "›",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoAlertText,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        val solicitados = allRecords.filter { it.status == "SOLICITACAO" }
        val aprovados = allRecords.filter { it.status == "APROVACAO" }
        val efetivados = allRecords.filter { it.status == "EFETIVADO" }

        // Row 1 & 2: Main Status Dashboard Bento Grid
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left prominent grid item: Solicitados (bg [#D3E4FF])
            Card(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab(2) },
                colors = CardDefaults.cardColors(containerColor = BentoSolicitadosBg),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = "Pendentes",
                        tint = BentoSolicitadosText,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = solicitados.size.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSolicitadosText
                        )
                        Text(
                            text = "SOLICITADOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSolicitadosText.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Right side containing Two stacked status cells
            Column(
                modifier = Modifier.weight(1.0f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Task 2: Aprovação cell (bg [#FFE08D])
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(2) },
                    colors = CardDefaults.cardColors(containerColor = BentoAprovacaoBg),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(BentoAprovacaoText, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = "Regra",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = aprovados.size.toString().padStart(2, '0'),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            Text(
                                "APROVAÇÃO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAprovacaoText
                            )
                        }
                    }
                }

                // Task 3: Efetivados cell (bg [#C2EFD0])
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(2) },
                    colors = CardDefaults.cardColors(containerColor = BentoEfetivadosBg),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(BentoEfetivadosText, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Check",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = efetivados.size.toString().padStart(2, '0'),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            Text(
                                "EFETIVADOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoEfetivadosText
                            )
                        }
                    }
                }
            }
        }

        // Row 4 & 5 Bento: Info & Export
        Row(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card: Histórico status block with rounded corner shape [28.dp]
            val lastRecord = allRecords.lastOrNull()
            val completionRatio = if (allRecords.isEmpty()) 0f else (efetivados.size.toFloat() / allRecords.size.toFloat())
            Card(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab(2) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Histórico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BentoTextPrimary
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lastRecord?.supplierName ?: "Nenhum forn.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${Math.round(completionRatio * 100)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                        
                        // ProgressBar matches bento styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(Color(0xFFE1E2EC), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (completionRatio > 0f) completionRatio else 0.01f)
                                    .fillMaxHeight()
                                    .background(BentoDeepBlueActionBg, CircleShape)
                            )
                        }

                        Text(
                            text = if (lastRecord != null) "Último: ${lastRecord.productName}" else "Nenhum registro",
                            fontSize = 10.sp,
                            color = Color(0xFF74777F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right Card: Exportar CSV styled in beautiful deep blue bg
            Card(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .clickable { onShareCsv() },
                colors = CardDefaults.cardColors(containerColor = BentoDeepBlueActionBg),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Exportar CSV",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Enviar via WhatsApp",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Row 6 Bento: Register Nova Solicitação (matches [#2E3033])
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onNavigateToTab(1) },
            colors = CardDefaults.cardColors(containerColor = BentoDarkActionBg),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NOVA SOLICITAÇÃO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 1: FORM (Bento styled Form)
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    onAddSuccess: () -> Unit,
    viewModel: ProductViewModel
) {
    var name by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var qtyString by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var isAlertEnabled by remember { mutableStateOf(false) }
    
    // Quick days chip selection
    var selectedDaysOffset by remember { mutableStateOf(30) } // Default 30 days
    var customDateInput by remember { mutableStateOf("") } // DD/MM/AAAA
    var customDateError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "Registrar Reposição",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = BentoTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Informe o produto que está em falta na gôndola/estoque.",
                fontSize = 13.sp,
                color = BentoTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        // Inputs styled with Bento outlines
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do Produto *") },
            placeholder = { Text("Ex: Arroz Agulhinha Tipo 1") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_product_name"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepBlueActionBg,
                unfocusedBorderColor = BentoOutline,
                focusedLabelColor = BentoDeepBlueActionBg
            )
        )

        OutlinedTextField(
            value = supplier,
            onValueChange = { supplier = it },
            label = { Text("Fornecedor / Distribuidor *") },
            placeholder = { Text("Ex: Tio João S/A") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_supplier"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepBlueActionBg,
                unfocusedBorderColor = BentoOutline,
                focusedLabelColor = BentoDeepBlueActionBg
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = qtyString,
                onValueChange = { qtyString = it.filter { char -> char.isDigit() } },
                label = { Text("Quantidade *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_quantity"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoDeepBlueActionBg,
                    unfocusedBorderColor = BentoOutline,
                    focusedLabelColor = BentoDeepBlueActionBg
                )
            )

            // Spinner/Buttons for increment/decrement quantites
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        val current = qtyString.toIntOrNull() ?: 1
                        if (current > 1) qtyString = (current - 1).toString()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(BentoSolicitadosBg, CircleShape)
                        .clip(CircleShape)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = BentoSolicitadosText)
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        val current = qtyString.toIntOrNull() ?: 0
                        qtyString = (current + 1).toString()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(BentoSolicitadosBg, CircleShape)
                        .clip(CircleShape)
                ) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BentoSolicitadosText)
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observações adicionais (Opcional)") },
            placeholder = { Text("Ex: urgente, alta procura") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("input_notes"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepBlueActionBg,
                unfocusedBorderColor = BentoOutline,
                focusedLabelColor = BentoDeepBlueActionBg
            )
        )

        // Expiration Alert system styled like a Bento Tile
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monitorar Vencimento",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Ligar alertas para o vencimento deste lote",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }
                    Switch(
                        checked = isAlertEnabled,
                        onCheckedChange = { isAlertEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BentoDeepBlueActionBg,
                            uncheckedTrackColor = BentoOutline
                        ),
                        modifier = Modifier.testTag("switch_vencimento")
                    )
                }

                AnimatedVisibility(visible = isAlertEnabled) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Previsão de Vencimento:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips choices matching Bento
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val daysChoices = listOf(
                                7 to "7 Dias",
                                15 to "15 Dias",
                                30 to "30 Dias",
                                90 to "90 Dias",
                                0 to "Digitar"
                            )

                            daysChoices.forEach { (days, label) ->
                                val selected = selectedDaysOffset == days
                                SuggestionChip(
                                    onClick = { selectedDaysOffset = days },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (selected) BentoSolicitadosBg else Color.Transparent,
                                        labelColor = if (selected) BentoSolicitadosText else BentoTextSecondary
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = if (selected) BentoSolicitadosText else BentoOutline
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedDaysOffset > 0) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, selectedDaysOffset)
                            val displayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                            Text(
                                text = "Alerta disparado na aba de Alertas por volta de: $displayDate",
                                fontSize = 12.sp,
                                color = BentoDeepBlueActionBg,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = customDateInput,
                                onValueChange = { input ->
                                    val cleaned = input.filter { it.isDigit() }
                                    val formatted = when {
                                        cleaned.length <= 2 -> cleaned
                                        cleaned.length <= 4 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
                                        else -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4, Math.min(cleaned.length, 8))}"
                                    }
                                    customDateInput = formatted
                                    customDateError = if (formatted.length == 10) {
                                        try {
                                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            sdf.isLenient = false
                                            sdf.parse(formatted)
                                            false
                                        } catch (e: Exception) {
                                            true
                                        }
                                    } else {
                                        false
                                    }
                                },
                                label = { Text("Data de Vencimento (DD/MM/AAAA)") },
                                placeholder = { Text("Ex: 31/12/2026") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_vencimento_manual"),
                                shape = RoundedCornerShape(14.dp),
                                isError = customDateError,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoDeepBlueActionBg,
                                    unfocusedBorderColor = BentoOutline
                                )
                            )
                            if (customDateError) {
                                Text(
                                    "Por favor insira uma data válida (ex: 25/12/2026)",
                                    color = BentoAlertAccent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit Button matching Bento Grid aesthetics
        val formIsValid = name.isNotBlank() && supplier.isNotBlank() && qtyString.toIntOrNull() ?: 0 > 0 &&
                (!isAlertEnabled || selectedDaysOffset > 0 || (customDateInput.length == 10 && !customDateError))

        Button(
            onClick = {
                if (formIsValid) {
                    var calculatedExp: Long? = null
                    if (isAlertEnabled) {
                        if (selectedDaysOffset > 0) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, selectedDaysOffset)
                            calculatedExp = cal.timeInMillis
                        } else if (customDateInput.length == 10) {
                            try {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                calculatedExp = sdf.parse(customDateInput)?.time
                            } catch (e: Exception) {
                            }
                        }
                    }

                    viewModel.addRecord(
                        name = name.trim(),
                        supplier = supplier.trim(),
                        quantity = qtyString.toIntOrNull() ?: 1,
                        notes = notes.trim(),
                        expirationDate = calculatedExp
                    )

                    name = ""
                    supplier = ""
                    qtyString = "1"
                    notes = ""
                    isAlertEnabled = false
                    
                    onAddSuccess()
                }
            },
            enabled = formIsValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BentoDarkActionBg,
                disabledContainerColor = BentoOutline.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Solicitação", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

// -----------------------------------------------------------------
// TAB 2: HISTORY & LOGS LIST (Bento Grid Theme)
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<ProductRecord>,
    searchQuery: String,
    statusFilter: String?,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onApprove: (Int) -> Unit,
    onComplete: (Int) -> Unit,
    onDelete: (ProductRecord) -> Unit,
    onShareCsv: () -> Unit,
    viewModel: ProductViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Histórico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = BentoTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Acompanhamento de Processos",
                    fontSize = 13.sp,
                    color = BentoTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // High-contrast, beautifully rounded direct share button in WhatsApp Green
            IconButton(
                onClick = onShareCsv,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF25D366), RoundedCornerShape(14.dp))
                    .testTag("export_whatsapp_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Exportar WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bento Outlined Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Pesquisar produto ou fornecedor...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepBlueActionBg,
                unfocusedBorderColor = BentoOutline,
                focusedLabelColor = BentoDeepBlueActionBg,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Status filters styled as beautiful Bento Pills
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                null to "Todos",
                "SOLICITACAO" to "Solicitado",
                "APROVACAO" to "Aprovado",
                "EFETIVADO" to "Efetivado"
            )

            filters.forEach { (status, label) ->
                val isSelected = statusFilter == status
                
                val (chipColor, labelColor) = if (isSelected) {
                    when (status) {
                        "SOLICITACAO" -> BentoSolicitadosBg to BentoSolicitadosText
                        "APROVACAO" -> BentoAprovacaoBg to BentoAprovacaoText
                        "EFETIVADO" -> BentoEfetivadosBg to BentoEfetivadosText
                        else -> BentoDarkActionBg to Color.White
                    }
                } else {
                    Color.White to BentoTextSecondary
                }

                SuggestionChip(
                    onClick = { onStatusFilterChange(status) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = chipColor,
                        labelColor = labelColor
                    ),
                    modifier = Modifier.testTag("filter_chip_${status ?: "todos"}"),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = if (isSelected) labelColor else BentoOutline
                    )
                )
            }
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Vazio",
                        tint = BentoOutline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhum fluxo encontrado",
                        color = BentoTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryItemCard(
                        record = record,
                        onApprove = { onApprove(record.id) },
                        onComplete = { onComplete(record.id) },
                        onDelete = { onDelete(record) },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Interactive WhatsApp quick actions card at the bottom
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BentoSolicitadosBg),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable { onShareCsv() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF25D366), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Exportar para Gerência",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSolicitadosText
                    )
                    Text(
                        "Enviar planilha em formato CSV via WhatsApp",
                        fontSize = 11.sp,
                        color = BentoSolicitadosText.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    record: ProductRecord,
    onApprove: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    viewModel: ProductViewModel
) {
    val (statusLabel, statusColor, statusBg) = when (record.status) {
        "SOLICITACAO" -> Triple("Solicitado", BentoSolicitadosText, BentoSolicitadosBg)
        "APROVACAO" -> Triple("Aprovado", BentoAprovacaoText, BentoAprovacaoBg)
        "EFETIVADO" -> Triple("Efetivado", BentoEfetivadosText, BentoEfetivadosBg)
        else -> Triple("Desconhecido", BentoTextSecondary, BentoOutline.copy(alpha = 0.5f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and qty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BentoTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Fornecedor",
                            tint = BentoTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Forn: ${record.supplierName}",
                            fontSize = 13.sp,
                            color = BentoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Rounded batch label chip
                Box(
                    modifier = Modifier
                        .background(BentoSolicitadosBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${record.quantityNeeded} un",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BentoSolicitadosText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates and Custom expiration alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reg: ${viewModel.formatDate(record.registrationDate)}",
                    fontSize = 12.sp,
                    color = BentoTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                if (record.expirationDate != null) {
                    val remaining = record.expirationDate - System.currentTimeMillis()
                    val remainingDays = (remaining / (24 * 60 * 60 * 1000)).toInt()

                    val (expColor, expBg, expLabel) = when {
                        remainingDays < 0 -> Triple(BentoAlertAccent, BentoAlertBg, "Vencido")
                        remainingDays <= 7 -> Triple(BentoAlertAccent, BentoAlertBg, "Vence em $remainingDays d!")
                        remainingDays <= 30 -> Triple(BentoAprovacaoBg, BentoAprovacaoBg.copy(alpha = 0.3f), "Vence em $remainingDays d")
                        else -> Triple(BentoEfetivadosBg, BentoEfetivadosBg.copy(alpha = 0.3f), "Vence em $remainingDays d")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(expBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (remainingDays <= 7) BentoAlertAccent else BentoTextPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = expLabel,
                            fontSize = 11.sp,
                            color = if (remainingDays <= 7) BentoAlertAccent else BentoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BentoBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = record.notes,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = BentoTextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BentoOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer actions: Complete status transition buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left badge - Current phase
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor
                    )
                }

                // Interactive flow controllers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (record.status) {
                        "SOLICITACAO" -> {
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = BentoAprovacaoBg),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("action_approve_${record.id}"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoAprovacaoText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprovar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoAprovacaoText)
                            }
                        }
                        "APROVACAO" -> {
                            Button(
                                onClick = onComplete,
                                colors = ButtonDefaults.buttonColors(containerColor = BentoEfetivadosBg),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("action_complete_${record.id}"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoEfetivadosText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chegou", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoEfetivadosText)
                            }
                        }
                        "EFETIVADO" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Efetivado",
                                    tint = BentoEfetivadosText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Efetivado em ${viewModel.formatDate(record.completionDate)}",
                                    fontSize = 11.sp,
                                    color = BentoEfetivadosText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Rounded trash red icon button for clean layout
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(BentoAlertBg, CircleShape)
                            .clip(CircleShape)
                            .testTag("action_delete_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar",
                            tint = BentoAlertAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 3: EXPIRATION ALERTS SCREEN (Bento styled grid)
// -----------------------------------------------------------------
@Composable
fun AlertsScreen(
    nearExpirationRecords: List<ProductRecord>,
    viewModel: ProductViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "Vencimentos",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = BentoTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Produtos e Lotes Próximos do Vencimento",
                fontSize = 13.sp,
                color = BentoTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        if (nearExpirationRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(BentoEfetivadosBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sem alertas",
                            tint = BentoEfetivadosText,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Excelente! Nenhum lote de produto vencendo nos próximos 30 dias.",
                        color = BentoTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nearExpirationRecords) { record ->
                    val remaining = (record.expirationDate ?: 0L) - System.currentTimeMillis()
                    val remainingDays = (remaining / (24 * 60 * 60 * 1000)).toInt()

                    val (badgeText, badgeColor, badgeBg, cardBg) = when {
                        remainingDays < 0 -> Quadruple("VENCIDO", BentoAlertAccent, BentoAlertBg, BentoAlertBg.copy(alpha = 0.3f))
                        remainingDays <= 7 -> Quadruple("CRÍTICO", BentoAlertAccent, BentoAlertBg, BentoAlertBg.copy(alpha = 0.3f))
                        remainingDays <= 15 -> Quadruple("ALERTA", BentoTextPrimary, BentoAprovacaoBg, BentoAprovacaoBg.copy(alpha = 0.3f))
                        else -> Quadruple("ATENÇÃO", BentoEfetivadosText, BentoEfetivadosBg, BentoEfetivadosBg.copy(alpha = 0.3f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoOutline),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BentoTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Forn: ${record.supplierName} • Falta Qtd: ${record.quantityNeeded}",
                                    fontSize = 12.sp,
                                    color = BentoTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Vencimento: ${viewModel.formatDate(record.expirationDate)} (" + 
                                            (if (remainingDays < 0) "Vencido há ${-remainingDays} dias" else "Faltam $remainingDays dias") + ")",
                                    fontSize = 12.sp,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple Quadruple helper class
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}
