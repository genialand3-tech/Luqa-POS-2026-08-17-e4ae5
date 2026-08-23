package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Product
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSecondaryContainer
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.example.viewmodel.LuqaViewModel

@Composable
fun InventarioScreen(
    viewModel: LuqaViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categoriesWithAll by viewModel.categoriesWithAll.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val outOfStockProducts by viewModel.outOfStockProducts.collectAsState()
    val context = LocalContext.current
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importProductsFromCsv(context, it) { resultMsg ->
                Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    var showClearInventoryDialog by remember { mutableStateOf(false) }
    var selectedDeleteOption by remember { mutableStateOf("inactive") } // inactive, out_of_stock, or both

    if (showClearInventoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearInventoryDialog = false },
            title = { Text(text = "Limpiar Inventario", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Selecciona qué tipo de productos obsoletos deseas eliminar:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(
                        "inactive" to "Productos inactivos",
                        "out_of_stock" to "Productos sin stock (Stock 0)",
                        "both" to "Inactivos y sin stock"
                    ).forEach { (opt, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDeleteOption = opt }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedDeleteOption == opt,
                                onClick = { selectedDeleteOption = opt },
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
                        viewModel.deleteObsoleteProducts(selectedDeleteOption)
                        showClearInventoryDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showClearInventoryDialog = false }
                ) {
                    Text("Cancelar", color = Color(0xFF434653))
                }
            }
        )
    }

    var showCsvFormatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
    ) {
        // Top Mobile App Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inventario Veloz",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191B22),
                        maxLines = 1
                    )
                    Text(
                        text = "Gestión de productos y stock",
                        fontSize = 12.sp,
                        color = Color(0xFF434653),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showClearInventoryDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar inventario",
                            tint = Color.Red
                        )
                    }
                    IconButton(
                        onClick = {
                        viewModel.exportInventoryToCsv(context) { uri ->
                            if (uri != null) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Exportar Inventario"))
                            } else {
                                Toast.makeText(context, "Error al exportar inventario", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LuqaPrimary.copy(alpha = 0.1f)),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = LuqaPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Exportar CSV"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showCsvFormatDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LuqaPrimary.copy(alpha = 0.1f)),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = LuqaPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.UploadFile,
                        contentDescription = "Importar CSV"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.openAddProductModal() },
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("btn_add_product"),
                    colors = ButtonDefaults.buttonColors(containerColor = LuqaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = LuqaOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nuevo Producto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaOnPrimary,
                        maxLines = 1
                    )
                }
            }

            }
            // Low Inventory Alert Banner
            if (lowStockProducts.isNotEmpty() || outOfStockProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_alert_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD8A8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA580C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Alerta de Stock Crítico!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412)
                            )
                            val details = buildString {
                                if (outOfStockProducts.isNotEmpty()) append("${outOfStockProducts.size} Agotados")
                                if (outOfStockProducts.isNotEmpty() && lowStockProducts.isNotEmpty()) append(" • ")
                                if (lowStockProducts.isNotEmpty()) append("${lowStockProducts.size} Bajo Stock")
                            }
                            Text(
                                text = details,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC2410C)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEA580C))
                                .clickable {
                                    if (outOfStockProducts.isNotEmpty()) {
                                        viewModel.setCategory("🚫 Agotados (${outOfStockProducts.size})")
                                    } else {
                                        viewModel.setCategory("⚠️ Bajo Stock (${lowStockProducts.size})")
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Filtrar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar en inventario...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color(0xFF434653)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("inventory_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFAF8FF),
                    unfocusedContainerColor = Color(0xFFFAF8FF),
                    focusedBorderColor = LuqaPrimary,
                    unfocusedBorderColor = Color(0xFFE2E2EB)
                )
            )

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manage Categories Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(LuqaPrimary.copy(alpha = 0.12f))
                        .clickable { viewModel.openManageCategoriesModal() }
                        .padding(horizontal = 12.dp)
                        .testTag("btn_manage_categories_inv"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Category,
                            contentDescription = null,
                            tint = LuqaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Categorías",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaPrimary,
                            maxLines = 1
                        )
                    }
                }

                categoriesWithAll.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) LuqaPrimary else LuqaSurfaceContainerLow)
                            .clickable { viewModel.setCategory(cat) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) LuqaOnPrimary else Color(0xFF191B22),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE2E2EB))

        // Product Cards Grid (2 columns on mobile)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F9))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                InventoryProductCard(
                    product = product,
                    onEditClick = { viewModel.openEditProductModal(product) }
                )
            }
        }
    }

    if (showCsvFormatDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCsvFormatDialog = false },
            title = { Text(text = "Formato de Importación CSV") },
            text = {
                Column {
                    Text("Para importar productos masivamente, asegúrate de que el archivo CSV tenga las siguientes columnas en este orden exacto:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Nombre\n2. Categoria\n3. Costo\n4. Precio\n5. Stock\n6. SKU (Opcional)\n7. CodigoBarras (Opcional)",
                        fontWeight = FontWeight.Bold,
                        color = LuqaPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Los primeros 5 campos son obligatorios. Las cantidades y precios deben ser números. El sistema no importará el archivo si detecta errores, protegiendo tus datos actuales.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showCsvFormatDialog = false
                    csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "*/*"))
                }) {
                    Text("Seleccionar Archivo")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCsvFormatDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InventoryProductCard(
    product: Product,
    onEditClick: () -> Unit
) {
    val (borderColor, cardBg) = when {
        !product.isActive -> Pair(Color(0xFFFCA5A5), Color(0xFFFAF9F9))
        product.isOutOfStock -> Pair(Color(0xFFEF4444), Color(0xFFFEF2F2))
        product.isLowStock -> Pair(Color(0xFFF59E0B), Color(0xFFFFFBEB))
        else -> Pair(Color(0xFFE2E2EB), Color.White)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(product.placeholderColorHex)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (product.isActive) 1.0f else 0.5f
                    )
                } else if (product.placeholderText != null) {
                    Text(
                        text = product.placeholderText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isActive) LuqaPrimary else Color.Gray
                    )
                }

                // Status Badge on Top Left (Vigente / Bajo Stock / Agotado / No Vigente)
                val (badgeBg, badgeFg, badgeText, badgeIcon) = when {
                    !product.isActive -> Quadruple(Color(0xFFDC2626), Color.White, "NO VIGENTE", Icons.Filled.Block)
                    product.isOutOfStock -> Quadruple(Color(0xFFDC2626), Color.White, "AGOTADO", Icons.Filled.ErrorOutline)
                    product.isLowStock -> Quadruple(Color(0xFFD97706), Color.White, "BAJO STOCK", Icons.Filled.WarningAmber)
                    else -> Quadruple(Color(0xFF16A34A), Color.White, "VIGENTE", Icons.Filled.CheckCircle)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg
                        )
                    }
                }

                // Category Badge Overlay on Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LuqaPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaOnPrimary
                    )
                }
            }

            // Title & Category
            Column {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (product.isActive) Color(0xFF191B22) else Color(0xFF71717A)
                )
                Text(
                    text = "SKU: ${product.sku.ifBlank { "N/A" }}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF737784)
                )
            }

            // Stock & Value Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val stockBg = when {
                    product.isOutOfStock -> Color(0xFFFEE2E2)
                    product.isLowStock -> Color(0xFFFEF3C7)
                    else -> Color(0xFFF3F3FC)
                }
                val stockFg = when {
                    product.isOutOfStock -> Color(0xFFDC2626)
                    product.isLowStock -> Color(0xFFD97706)
                    else -> Color(0xFF191B22)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(stockBg)
                        .padding(6.dp)
                ) {
                    Column {
                        Text(text = "Stock", fontSize = 10.sp, color = Color(0xFF737784), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (product.isOutOfStock) "0 uds (Agotado)" else "${product.stock} uds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockFg
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF3F3FC))
                        .padding(6.dp)
                ) {
                    Column {
                        Text(text = "Precio", fontSize = 10.sp, color = Color(0xFF737784), fontWeight = FontWeight.SemiBold)
                        Text(text = String.format("S/ %.2f", product.price), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                    }
                }
            }

            // Margin Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LuqaSecondaryContainer.copy(alpha = 0.4f))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Margen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00714B))
                    Text(
                        text = String.format("%.1f%%", product.marginPercentage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaSecondary
                    )
                }
            }

            // Edit / Stock Management Button
            val isUrgentRestock = product.isOutOfStock || product.isLowStock
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("btn_edit_product_${product.id}"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUrgentRestock) Color(0xFFEA580C) else LuqaPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isUrgentRestock) "Reabastecer" else "Editar / Stock",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
