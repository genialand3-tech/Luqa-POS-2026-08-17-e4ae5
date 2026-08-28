package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.model.Product
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.File

@Composable
fun EditProductModal(
    product: Product,
    availableCategories: List<String> = listOf("Bebidas", "Comida", "Snacks", "Postres", "Despensa"),
    onDismiss: () -> Unit,
    onSave: (updatedProduct: Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var category by remember { mutableStateOf(product.category) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var sku by remember { mutableStateOf(product.sku) }
    var costText by remember { mutableStateOf(product.cost.toString()) }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    var stockText by remember { mutableStateOf(product.stock.toString()) }
    var minStockText by remember { mutableStateOf(product.minStockThreshold.toString()) }
    var addUnitsText by remember { mutableStateOf("0") }
    var isActive by remember { mutableStateOf(product.isActive) }

    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    val categories = availableCategories.ifEmpty { listOf("General") }

    // Camera and Photo Logic
    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }
    
    var showPhotoOptions by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(if(product.imageUrl != null) Uri.parse(product.imageUrl) else null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                photoUri = uri
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoUri = tempPhotoUri
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val tmpFile = File.createTempFile("prod_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Subir Foto") },
            text = { Text("Elige una opción para la foto del producto.") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) {
                    Text("Tomar Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("De Galería")
                }
            }
        )
    }

    // Calculated values
    val currentCost = costText.toDoubleOrNull() ?: 0.0
    val currentPrice = priceText.toDoubleOrNull() ?: 0.0
    val baseStock = stockText.toIntOrNull() ?: 0
    val addedStock = addUnitsText.toIntOrNull() ?: 0
    val totalStock = (baseStock + addedStock).coerceAtLeast(0)

    val marginPct = if (currentPrice > 0) ((currentPrice - currentCost) / currentPrice) * 100.0 else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Administrar Inventario",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaPrimary
                        )
                        Text(
                            text = product.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF434653)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_edit_product")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF434653)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Photo Upload
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LuqaSurfaceContainerLow)
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFC3C6D5),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showPhotoOptions = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto",
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                contentScale = ContentScale.Crop,
                                placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = null,
                                    tint = LuqaPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Actualizar Foto del Producto",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuqaPrimary
                                )
                            }
                        }
                    }

                    // SECTION 1: ESTADO DEL PRODUCTO / VIGENCIA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Estado de Vigencia (SKU)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF166534) else Color(0xFF991B1B)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Vigente Option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) Color(0xFF16A34A) else Color.White)
                                        .border(
                                            1.dp,
                                            if (isActive) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { isActive = true }
                                        .padding(vertical = 10.dp, horizontal = 8.dp)
                                        .testTag("btn_status_vigente"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isActive) Color.White else Color(0xFF16A34A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Vigente (Activo)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color.White else Color(0xFF166534)
                                        )
                                    }
                                }

                                // Dado de Baja Option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (!isActive) Color(0xFFDC2626) else Color.White)
                                        .border(
                                            1.dp,
                                            if (!isActive) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { isActive = false }
                                        .padding(vertical = 10.dp, horizontal = 8.dp)
                                        .testTag("btn_status_baja"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Block,
                                            contentDescription = null,
                                            tint = if (!isActive) Color.White else Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Dado de baja",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isActive) Color.White else Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isActive)
                                    "✓ Producto activo visible en la pantalla de Ventas para cobrar a clientes."
                                else
                                    "⚠ Retirado por baja rotación. Ya no estará disponible para venta en caja.",
                                fontSize = 11.sp,
                                color = if (isActive) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }

                    // SECTION 2: PRECIO Y COSTO (MODIFICACIÓN DE PRECIOS POR AUMENTO)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = LuqaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Actualizar Precios / Costos",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Precio de Venta (S/)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    OutlinedTextField(
                                        value = priceText,
                                        onValueChange = { priceText = it },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        visualTransformation = ThousandSeparatorVisualTransformation(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edit_product_price"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuqaPrimary,
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Costo Producto (S/)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    OutlinedTextField(
                                        value = costText,
                                        onValueChange = { costText = it },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        visualTransformation = ThousandSeparatorVisualTransformation(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edit_product_cost"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuqaPrimary,
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Margin preview indicator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Margen de Ganancia estimado:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = String.format("%.1f%%", marginPct),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (marginPct > 0) Color(0xFF15803D) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    // SECTION 3: REPOSICIÓN DE STOCK / ADICIONAR UNIDADES
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Inventory2,
                                    contentDescription = null,
                                    tint = LuqaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Reposición y Control de Stock",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Stock Actual",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    OutlinedTextField(
                                        value = stockText,
                                        onValueChange = { stockText = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edit_current_stock"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuqaPrimary,
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "+ Adicionar Unidades",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuqaPrimary
                                    )
                                    OutlinedTextField(
                                        value = addUnitsText,
                                        onValueChange = { addUnitsText = it },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edit_add_units"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuqaPrimary,
                                            unfocusedBorderColor = LuqaPrimary,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Quick addition chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rápido:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold
                                )

                                listOf(5, 10, 20, 50).forEach { amount ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LuqaPrimary.copy(alpha = 0.1f))
                                            .clickable {
                                                val currentAdded = addUnitsText.toIntOrNull() ?: 0
                                                addUnitsText = (currentAdded + amount).toString()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$amount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LuqaPrimary
                                        )
                                    }
                                }
                            }

                            // New Total Stock summary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nuevo Stock Total resultantes:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "$totalStock unidades",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuqaPrimary
                                )
                            }

                            // Minimum Stock Alert Threshold Field
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Umbral Mínimo para Alerta de Bajo Stock",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                OutlinedTextField(
                                    value = minStockText,
                                    onValueChange = { minStockText = it },
                                    placeholder = { Text("5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_edit_min_stock_threshold"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuqaPrimary,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Text(
                                    text = "Se mostrará una alerta visual cuando el stock sea menor o igual a este número.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // SECTION 4: DATOS GENERALES (Nombre, Categoría, SKU, Barcode)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Nombre del Producto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_product_name"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuqaPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                        
                        Text(
                            text = "Código de Barras",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            trailingIcon = {
                                IconButton(onClick = {
                                    scanner.startScan()
                                        .addOnSuccessListener { bc ->
                                            bc.rawValue?.let { barcode = it }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error al escanear: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCodeScanner,
                                        contentDescription = "Barcode",
                                        tint = LuqaPrimary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_product_barcode"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuqaPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Categoría",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandedCategoryDropdown = true }
                                            .testTag("input_edit_product_category"),
                                        shape = RoundedCornerShape(10.dp),
                                        trailingIcon = {
                                            IconButton(onClick = { expandedCategoryDropdown = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuqaPrimary,
                                            unfocusedBorderColor = Color(0xFFCBD5E1)
                                        )
                                    )

                                    DropdownMenu(
                                        expanded = expandedCategoryDropdown,
                                        onDismissRequest = { expandedCategoryDropdown = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat, fontSize = 14.sp) },
                                                onClick = {
                                                    category = cat
                                                    expandedCategoryDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "SKU",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                OutlinedTextField(
                                    value = sku,
                                    onValueChange = { sku = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_edit_product_sku"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuqaPrimary,
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuqaSurfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cancel_edit_product")
                    ) {
                        Text(
                            text = "Cancelar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF191B22),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && currentPrice > 0) {
                                val updated = product.copy(
                                    name = name,
                                    category = category,
                                    cost = currentCost,
                                    price = currentPrice,
                                    stock = totalStock,
                                    minStockThreshold = minStockText.toIntOrNull() ?: 5,
                                    sku = sku,
                                    barcode = barcode,
                                    isActive = isActive,
                                    imageUrl = photoUri?.toString()
                                )
                                onSave(updated)
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_save_edit_product"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuqaPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            tint = LuqaOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar Cambios",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaOnPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
