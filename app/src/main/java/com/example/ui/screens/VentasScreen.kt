package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.viewmodel.NavDestination
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.example.viewmodel.LuqaViewModel

@Composable
fun VentasScreen(
    viewModel: LuqaViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoriesWithAll by viewModel.categoriesWithAll.collectAsState()

    val totalItems = cart.sumOf { it.quantity }
    val total = cart.sumOf { it.totalPrice }

    // Mobile state for expanded ticket panel
    var isCartExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(cart) {
        if (cart.isEmpty()) {
            isCartExpanded = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
    ) {
        // Main Smartphone Catalog Column
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App & Search Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Brand Header Row
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
                                .clip(CircleShape)
                                .background(LuqaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Luqa POS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaPrimary
                        )
                    }

                    val criticalCount by viewModel.criticalStockCount.collectAsState()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (criticalCount > 0) {
                            Surface(
                                onClick = { viewModel.navigateTo(NavDestination.INVENTARIO) },
                                shape = CircleShape,
                                color = Color(0xFFFFF7ED),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA580C))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WarningAmber,
                                        contentDescription = "Alertas de Stock",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$criticalCount Alertas",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEA580C)
                                    )
                                }
                            }
                        }

                        if (totalItems > 0) {
                            Surface(
                                onClick = { isCartExpanded = true },
                                shape = CircleShape,
                                color = LuqaPrimary.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LuqaPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = LuqaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$totalItems | S/ ${String.format("%.2f", total)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuqaPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar + QR Scanner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar productos...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF434653)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LuqaSurfaceContainerLow,
                            unfocusedContainerColor = LuqaSurfaceContainerLow,
                            focusedBorderColor = LuqaPrimary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scanner = androidx.compose.runtime.remember {
                        com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context)
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuqaSurfaceContainerLow)
                            .clickable {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        barcode.rawValue?.let {
                                            viewModel.setSearchQuery(it)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        android.widget.Toast.makeText(context, "Error al escanear: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan",
                            tint = Color(0xFF434653)
                        )
                    }
                }

                // Category Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoriesWithAll.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) LuqaPrimary else LuqaSurfaceContainerLow)
                                .clickable { viewModel.setCategory(cat) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) LuqaOnPrimary else Color(0xFF191B22)
                            )
                        }
                    }

                    // Manage Categories Chip Button
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(LuqaPrimary.copy(alpha = 0.12f))
                            .clickable { viewModel.openManageCategoriesModal() }
                            .padding(horizontal = 12.dp)
                            .testTag("btn_manage_categories_ventas"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Categorías",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E2EB))

            // Product Grid (2 columns on mobile)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF4F7F9))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts.filter { it.isActive }, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { viewModel.addToCart(product) }
                    )
                }
            }

            // Sticky Bottom Cart Bar (shows when items exist)
            if (cart.isNotEmpty() && !isCartExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCartExpanded = true },
                    color = Color.White,
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    .clip(CircleShape)
                                    .background(LuqaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Ticket #042 ($totalItems ítems)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF434653)
                                )
                                Text(
                                    text = String.format("S/ %.2f", total),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191B22)
                                )
                            }
                        }

                        Button(
                            onClick = { isCartExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LuqaSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Cobrar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandLess,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expanded Mobile Cart / Ticket Sheet Overlay
        AnimatedVisibility(
            visible = isCartExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mobile Ticket Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAF8FF))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { isCartExpanded = false }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Volver al Catálogo",
                                    tint = LuqaPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = LuqaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Ticket #042",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191B22)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.clearCart() },
                            modifier = Modifier.testTag("btn_clear_cart")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Vaciar Ticket",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E2EB))

                    // Ticket Items List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cart, key = { it.product.id }) { cartItem ->
                            CartItemRow(
                                cartItem = cartItem,
                                onIncrement = { viewModel.addToCart(cartItem.product) },
                                onDecrement = { viewModel.decrementCartProduct(cartItem.product.id) }
                            )
                        }
                    }

                    // Payment Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Total a Pagar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191B22)
                            )
                            Text(
                                text = String.format("S/ %.2f", total),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191B22)
                            )
                        }

                        // EFECTIVO (Full Width Green Button)
                        Button(
                            onClick = { viewModel.processPayment(PaymentMethod.EFECTIVO) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("btn_pay_cash"),
                            colors = ButtonDefaults.buttonColors(containerColor = LuqaSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Efectivo",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // TARJETA & TRANSF. Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.processPayment(PaymentMethod.TARJETA) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_pay_card"),
                                colors = ButtonDefaults.buttonColors(containerColor = LuqaSecondary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Tarjeta", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            Button(
                                onClick = { viewModel.processPayment(PaymentMethod.TRANSFERENCIA) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_pay_transfer"),
                                colors = ButtonDefaults.buttonColors(containerColor = LuqaSecondary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Transf.", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val borderColor = when {
        product.isOutOfStock -> Color(0xFFEF4444)
        product.isLowStock -> Color(0xFFF59E0B)
        else -> Color(0xFFE2E2EB)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isOutOfStock) Color(0xFFFEF2F2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(product.placeholderColorHex)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (product.isOutOfStock) 0.4f else 1.0f,
                        placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                    )
                } else if (product.placeholderText != null) {
                    Text(
                        text = product.placeholderText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuqaPrimary
                    )
                }

                // Low Stock / Out of Stock Badge Overlay
                if (product.isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDC2626))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🚫 AGOTADO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (product.isLowStock) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFD97706))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚠️ Quedan ${product.stock}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF191B22)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("S/ %.2f", product.price),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuqaPrimary
                )

                Text(
                    text = if (product.isOutOfStock) "Agotado" else "Stock: ${product.stock}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        product.isOutOfStock -> Color(0xFFDC2626)
                        product.isLowStock -> Color(0xFFD97706)
                        else -> Color(0xFF64748B)
                    }
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8FF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = cartItem.product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF191B22),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = String.format("S/ %.2f", cartItem.totalPrice),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191B22)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("S/ %.2f c/u", cartItem.product.price),
                    fontSize = 13.sp,
                    color = Color(0xFF434653)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E2EB))
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Restar",
                            tint = Color(0xFF191B22)
                        )
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Sumar",
                            tint = LuqaPrimary
                        )
                    }
                }
            }
        }
    }
}
