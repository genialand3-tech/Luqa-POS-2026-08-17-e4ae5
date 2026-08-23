package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Inventory2
import com.example.model.PaymentMethod
import com.example.model.RefundRecord
import com.example.model.SaleRecord
import com.example.ui.components.EditTicketModal
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.example.viewmodel.LuqaViewModel

@Composable
fun HistorialScreen(
    viewModel: LuqaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val refunds by viewModel.refunds.collectAsState()
    val allProducts by viewModel.products.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Tickets, 1 = Devoluciones
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFrameFilter by remember { mutableStateOf("TODOS") }
    var selectedSaleForEdit by remember { mutableStateOf<SaleRecord?>(null) }

    val now = System.currentTimeMillis()
    val millisInDay = 86400000L

    val filteredSales = sales.filter { sale ->
        val matchesSearch = if (searchQuery.isBlank()) true
        else {
            sale.operationNumber.contains(searchQuery, ignoreCase = true) ||
                    sale.items.any { it.product.name.contains(searchQuery, ignoreCase = true) }
        }

        val diff = now - sale.dateMillis
        val matchesTime = when (selectedTimeFrameFilter) {
            "DIA" -> diff <= millisInDay
            "SEMANA" -> diff <= 7 * millisInDay
            "MES" -> diff <= 30 * millisInDay
            else -> true
        }

        matchesSearch && matchesTime
    }

    val filteredRefunds = refunds.filter { refund ->
        val matchesSearch = if (searchQuery.isBlank()) true
        else {
            refund.operationNumber.contains(searchQuery, ignoreCase = true) ||
                    refund.reason.contains(searchQuery, ignoreCase = true) ||
                    refund.returnedItems.any { it.product.name.contains(searchQuery, ignoreCase = true) }
        }

        val diff = now - refund.dateMillis
        val matchesTime = when (selectedTimeFrameFilter) {
            "DIA" -> diff <= millisInDay
            "SEMANA" -> diff <= 7 * millisInDay
            "MES" -> diff <= 30 * millisInDay
            else -> true
        }

        matchesSearch && matchesTime
    }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var selectedDeleteTimeFrame by remember { mutableStateOf(30) } // days

    if (showClearHistoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(text = "Eliminar Datos Antiguos", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Selecciona la antigüedad de los registros de ventas y devoluciones que deseas eliminar:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(
                        30 to "Más de 1 mes",
                        90 to "Más de 3 meses",
                        180 to "Más de 6 meses"
                    ).forEach { (days, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDeleteTimeFrame = days }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedDeleteTimeFrame == days,
                                onClick = { selectedDeleteTimeFrame = days },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val cutoffTime = System.currentTimeMillis() - (selectedDeleteTimeFrame * 86400000L)
                        viewModel.deleteSalesOlderThan(cutoffTime)
                        viewModel.deleteRefundsOlderThan(cutoffTime)
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showClearHistoryDialog = false }
                ) {
                    Text("Cancelar", color = Color(0xFF434653))
                }
            }
        )
    }

    val totalVentas = filteredSales.sumOf { it.totalAmount }
    val totalDevoluciones = filteredRefunds.sumOf { it.totalRefunded }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Historial & Devoluciones",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191B22)
                )
                Text(
                    text = "Trazabilidad de ventas, devoluciones y reingreso de stock a inventario",
                    fontSize = 12.sp,
                    color = Color(0xFF434653)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                androidx.compose.material3.IconButton(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar todo",
                        tint = Color.Red
                    )
                }
                androidx.compose.material3.IconButton(
                    onClick = {
                    viewModel.exportSalesHistory(context) { uri ->
                        if (uri != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Exportar Historial"))
                        } else {
                            Toast.makeText(context, "Error al exportar historial", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LuqaPrimary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Exportar CSV",
                    tint = LuqaPrimary
                )
            }
            }
        }

        // Tab Selector (Tickets Emitidos vs Devoluciones)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E2EB))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 0) LuqaPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .testTag("tab_tickets_emitidos"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = if (selectedTab == 0) LuqaOnPrimary else Color(0xFF434653),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tickets (${sales.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) LuqaOnPrimary else Color(0xFF434653)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 1) Color(0xFFD32F2F) else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .testTag("tab_historial_devoluciones"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssignmentReturn,
                        contentDescription = null,
                        tint = if (selectedTab == 1) Color.White else Color(0xFF434653),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Devoluciones (${refunds.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else Color(0xFF434653)
                    )
                }
            }
        }

        // Summary Bento Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedTab == 0) "Total Tickets" else "Total Devoluciones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF737784)
                    )
                    Text(
                        text = if (selectedTab == 0) "${filteredSales.size} tickets" else "${filteredRefunds.size} registros",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) LuqaPrimary else Color(0xFFD32F2F)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedTab == 0) "Monto Facturado" else "Monto Devuelto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF737784)
                    )
                    Text(
                        text = "S/ ${String.format("%.2f", if (selectedTab == 0) totalVentas else totalDevoluciones)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) LuqaSecondary else Color(0xFFD32F2F)
                    )
                }
            }
        }

        // Timeframe Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("TODOS" to "Todos", "DIA" to "Día", "SEMANA" to "Semana", "MES" to "Mes").forEach { (code, label) ->
                val isSelected = selectedTimeFrameFilter == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) LuqaPrimary else Color(0xFFE2E2EB))
                        .clickable { selectedTimeFrameFilter = code }
                        .padding(horizontal = 8.dp)
                        .testTag("filter_historial_${code.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LuqaOnPrimary else Color(0xFF434653)
                    )
                }
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (selectedTab == 0) "Buscar por TRX # o producto..." else "Buscar TRX #, motivo o producto...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color(0xFF737784)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("input_search_historial"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = LuqaPrimary,
                unfocusedBorderColor = Color(0xFFE2E2EB)
            )
        )

        HorizontalDivider(color = Color(0xFFE2E2EB))

        if (selectedTab == 0) {
            // TAB 0: Tickets List
            if (filteredSales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFFC3C6D5),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron tickets" else "Aún no hay ventas registradas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF737784)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSales, key = { it.id }) { sale ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ticket_card_${sale.operationNumber}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Card Top Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LuqaPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.ReceiptLong,
                                                contentDescription = null,
                                                tint = LuqaPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = sale.operationNumber,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF191B22)
                                            )
                                            Text(
                                                text = sale.timestamp,
                                                fontSize = 11.sp,
                                                color = Color(0xFF737784)
                                            )
                                        }
                                    }

                                    // Payment Method Badge
                                    val (methodBg, methodFg, iconVector) = when (sale.paymentMethod) {
                                        PaymentMethod.EFECTIVO -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Filled.Payments)
                                        PaymentMethod.TARJETA -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Filled.CreditCard)
                                        PaymentMethod.TRANSFERENCIA -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Filled.AccountBalance)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(methodBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = iconVector,
                                                contentDescription = null,
                                                tint = methodFg,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = sale.paymentMethod.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = methodFg
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F5))

                                // Product Items Summary
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    sale.items.forEach { cartItem ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${cartItem.quantity}x ${cartItem.product.name}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF434653)
                                            )
                                            Text(
                                                text = "S/ ${String.format("%.2f", cartItem.totalPrice)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF191B22)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F5))

                                // Card Footer with Total & Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Facturado",
                                            fontSize = 11.sp,
                                            color = Color(0xFF737784)
                                        )
                                        Text(
                                            text = "S/ ${String.format("%.2f", sale.totalAmount)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LuqaPrimary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { selectedSaleForEdit = sale },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("btn_edit_ticket_${sale.operationNumber}"),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LuqaPrimary),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Ver / Editar",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: Historial de Devoluciones (Traceability)
            if (filteredRefunds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AssignmentReturn,
                            contentDescription = null,
                            tint = Color(0xFFC3C6D5),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron devoluciones" else "Aún no se registran devoluciones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF737784)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRefunds, key = { it.id }) { refund ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("refund_card_${refund.operationNumber}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top Header with Red Devolución Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFEBEE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AssignmentReturn,
                                                contentDescription = null,
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = refund.operationNumber,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF191B22)
                                            )
                                            Text(
                                                text = "${refund.timestamp} • ${refund.paymentMethod.name}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF737784)
                                            )
                                        }
                                    }

                                    // Badge DEVOLUCIÓN
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEBEE))
                                            .border(1.dp, Color(0xFFEF9A9A), CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "DEVOLUCIÓN",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F5))

                                // Reason Callout Box (Trazabilidad)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF8E1))
                                        .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "📌 Motivo de la Devolución:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                        Text(
                                            text = refund.reason,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                }

                                // Inventory Restocked Callout Badge
                                val totalUnitsRestocked = refund.returnedItems.sumOf { it.quantity }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Inventory2,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "📦 Stock Reingresado: +$totalUnitsRestocked unids. agregadas a Inventario",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                // Returned Items List
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    refund.returnedItems.forEach { cartItem ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${cartItem.quantity}x ${cartItem.product.name}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF434653)
                                            )
                                            Text(
                                                text = "S/ ${String.format("%.2f", cartItem.totalPrice)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF191B22)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F5))

                                // Refund Total
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Reembolsado",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF737784)
                                    )
                                    Text(
                                        text = "- S/ ${String.format("%.2f", refund.totalRefunded)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal when ticket is selected for edit/devolución
    selectedSaleForEdit?.let { sale ->
        EditTicketModal(
            sale = sale,
            allProducts = allProducts,
            onDismiss = { selectedSaleForEdit = null },
            onSaveEdits = { saleId, updatedItems, reason ->
                viewModel.updateSaleItems(saleId, updatedItems, reason)
                Toast.makeText(context, "Ticket actualizado. Stock reingresado en Inventario.", Toast.LENGTH_SHORT).show()
                selectedSaleForEdit = null
            },
            onDeleteTicket = { saleId, reason ->
                viewModel.processRefund(saleId, reason)
                Toast.makeText(context, "Devolución procesada. Stock sumado al Inventario y registrado en Historial.", Toast.LENGTH_LONG).show()
                selectedSaleForEdit = null
            }
        )
    }
}
