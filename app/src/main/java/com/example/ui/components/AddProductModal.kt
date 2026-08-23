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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.File

@Composable
fun AddProductModal(
    availableCategories: List<String> = listOf("Bebidas", "Comida", "Snacks", "Postres", "Despensa"),
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, cost: Double, price: Double, stock: Int, sku: String, barcode: String, imageUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(availableCategories.firstOrNull() ?: "General") }
    var barcode by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("") }

    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    val categories = availableCategories.ifEmpty { listOf("General") }

    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    var showPhotoOptions by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.94f)
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
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregar Nuevo Producto",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_add_product")
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
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Image Upload Area Box
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
                                contentScale = ContentScale.Crop
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
                                    text = "Subir Foto del Producto",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuqaPrimary
                                )
                                Text(
                                    text = "PNG, JPG hasta 5MB",
                                    fontSize = 14.sp,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }
                    }

                    // Product Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Nombre del Producto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF191B22)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Ej. Inka Kola 500ml") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_product_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuqaPrimary,
                                unfocusedBorderColor = Color(0xFFC3C6D5)
                            )
                        )
                    }

                    // Category Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Categoría",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF191B22)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedCategoryDropdown = true }
                                    .testTag("input_product_category"),
                                shape = RoundedCornerShape(12.dp),
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
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )

                            DropdownMenu(
                                expanded = expandedCategoryDropdown,
                                onDismissRequest = { expandedCategoryDropdown = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 16.sp) },
                                        onClick = {
                                            category = cat
                                            expandedCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Barcode & SKU Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Código de Barras",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191B22)
                            )
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                placeholder = { Text("Escanear o ingresar") },
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
                                    .testTag("input_product_barcode"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuqaPrimary,
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SKU",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191B22)
                            )
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                placeholder = { Text("Opcional") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_product_sku"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuqaPrimary,
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )
                        }
                    }

                    // Cost, Price, Stock Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Costo (S/)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191B22)
                            )
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_product_cost"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuqaPrimary,
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Precio (S/)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191B22)
                            )
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_product_price"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuqaPrimary,
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Stock Inicial",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191B22)
                            )
                            OutlinedTextField(
                                value = stockText,
                                onValueChange = { stockText = it },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_product_stock"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuqaPrimary,
                                    unfocusedBorderColor = Color(0xFFC3C6D5)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                            .testTag("btn_cancel_add_product")
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
                            val costVal = costText.toDoubleOrNull() ?: 0.0
                            val priceVal = priceText.toDoubleOrNull() ?: 0.0
                            val stockVal = stockText.toIntOrNull() ?: 0
                            if (name.isNotBlank() && priceVal > 0) {
                                onSave(name, category, costVal, priceVal, stockVal, sku, barcode, photoUri?.toString())
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_save_add_product"),
                        colors = ButtonDefaults.buttonColors(containerColor = LuqaPrimary),
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
                            text = "Guardar",
                            fontSize = 15.sp,
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
