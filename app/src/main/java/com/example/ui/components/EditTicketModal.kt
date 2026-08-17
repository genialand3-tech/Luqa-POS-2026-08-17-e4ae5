package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CartItem
import com.example.model.Product
import com.example.model.SaleRecord
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSurfaceContainerLow

import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

@Composable
fun EditTicketModal(
    sale: SaleRecord,
    allProducts: List<Product>,
    onDismiss: () -> Unit,
    onSaveEdits: (saleId: String, updatedItems: List<CartItem>, reason: String) -> Unit,
    onDeleteTicket: (saleId: String, reason: String) -> Unit
) {
    var items by remember { mutableStateOf(sale.items) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddProductMenu by remember { mutableStateOf(false) }

    var selectedReason by remember { mutableStateOf("Producto defectuoso / dañado") }
    var customReasonText by remember { mutableStateOf("") }

    val presetReasons = listOf(
        "Producto defectuoso / dañado",
        "Error de compra / Cambio del cliente",
        "Producto vencido / mal estado",
        "Precio o cobro incorrecto",
        "Otro motivo"
    )

    val effectiveReason = if (selectedReason == "Otro motivo") {
        customReasonText.ifBlank { "Otro motivo no especificado" }
    } else selectedReason

    val newTotal = items.sumOf { it.totalPrice }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LuqaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = LuqaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ticket ${sale.operationNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191B22)
                            )
                            Text(
                                text = "${sale.timestamp} • ${sale.paymentMethod.name}",
                                fontSize = 12.sp,
                                color = Color(0xFF737784)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF191B22)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E2EB))

                // Section Title + Add Product Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Productos en este Ticket",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191B22)
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showAddProductMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = LuqaPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Añadir Producto",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuqaPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showAddProductMenu,
                            onDismissRequest = { showAddProductMenu = false }
                        ) {
                            allProducts.forEach { product ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${product.name} (S/ ${String.format("%.2f", product.price)})")
                                    },
                                    onClick = {
                                        val existingIndex = items.indexOfFirst { it.product.id == product.id }
                                        if (existingIndex >= 0) {
                                            val existing = items[existingIndex]
                                            items = items.toMutableList().apply {
                                                this[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                                            }
                                        } else {
                                            items = items + CartItem(product = product, quantity = 1)
                                        }
                                        showAddProductMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Item List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (items.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "El ticket no tiene productos.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF737784)
                                )
                            }
                        }
                    }

                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LuqaSurfaceContainerLow),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191B22),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "S/ ${String.format("%.2f", item.product.price)} c/u",
                                        fontSize = 12.sp,
                                        color = Color(0xFF737784)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                items = items.map {
                                                    if (it.product.id == item.product.id) it.copy(quantity = it.quantity - 1) else it
                                                }
                                            } else {
                                                items = items.filterNot { it.product.id == item.product.id }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Remove,
                                            contentDescription = "Reducir",
                                            tint = Color(0xFF434653)
                                        )
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191B22)
                                    )

                                    IconButton(
                                        onClick = {
                                            items = items.map {
                                                if (it.product.id == item.product.id) it.copy(quantity = it.quantity + 1) else it
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Aumentar",
                                            tint = LuqaPrimary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            items = items.filterNot { it.product.id == item.product.id }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E2EB))

                // Total Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Total",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191B22)
                    )
                    Text(
                        text = "S/ ${String.format("%.2f", newTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaPrimary
                    )
                }

                // Action Buttons Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onSaveEdits(sale.id, items, effectiveReason)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_save_ticket_edits"),
                        colors = ButtonDefaults.buttonColors(containerColor = LuqaPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar Cambios",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaOnPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_delete_ticket_devolucion"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Procesar Devolución / Anular Ticket",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog for Devolución with Reason Selector
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Procesar Devolución de Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ticket ${sale.operationNumber} • S/ ${String.format("%.2f", sale.totalAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191B22)
                    )

                    // Stock Callout Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📦 Reingreso de Inventario Automático",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            val totalUnits = sale.items.sumOf { it.quantity }
                            Text(
                                text = "Las $totalUnits unidades devueltas se sumarán inmediatamente a las existencias en la pestaña Inventario.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    // Reason Selector
                    Text(
                        text = "Seleccione el motivo de la devolución:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF191B22)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        presetReasons.forEach { reasonOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedReason = reasonOption }
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedReason == reasonOption),
                                    onClick = { selectedReason = reasonOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = LuqaPrimary)
                                )
                                Text(
                                    text = reasonOption,
                                    fontSize = 12.sp,
                                    color = Color(0xFF434653)
                                )
                            }
                        }
                    }

                    if (selectedReason == "Otro motivo") {
                        OutlinedTextField(
                            value = customReasonText,
                            onValueChange = { customReasonText = it },
                            placeholder = { Text("Escriba la razón de la devolución...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTicket(sale.id, effectiveReason)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Confirmar y Reingresar Stock", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = Color(0xFF191B22))
                }
            }
        )
    }
}
