package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import com.example.model.PaymentMethod
import com.example.ui.theme.LuqaOnPrimary
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSecondary
import com.example.ui.theme.LuqaSecondaryContainer
import com.example.viewmodel.LuqaViewModel

enum class CuadreTimeFrame(val chipLabel: String, val headerTitle: String, val subtitle: String) {
    DIA("Hoy (Día)", "Resumen Financiero del Día", "Últimas 24 horas"),
    SEMANA("Esta Semana", "Resumen Financiero Semanal", "Últimos 7 días"),
    MES("Este Mes", "Resumen Financiero Mensual", "Últimos 30 días")
}

@Composable
fun CuadreScreen(
    viewModel: LuqaViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    var selectedTimeFrame by remember { mutableStateOf(CuadreTimeFrame.DIA) }
    var selectedPaymentFilter by remember { mutableStateOf<PaymentMethod?>(null) }

    val now = System.currentTimeMillis()
    val millisInDay = 86400000L

    // Filter sales based on selected timeframe
    val timeFilteredSales = remember(sales, selectedTimeFrame) {
        sales.filter { sale ->
            val diff = now - sale.dateMillis
            when (selectedTimeFrame) {
                CuadreTimeFrame.DIA -> diff <= millisInDay
                CuadreTimeFrame.SEMANA -> diff <= 7 * millisInDay
                CuadreTimeFrame.MES -> diff <= 30 * millisInDay
            }
        }
    }

    // Filter sales based on selected payment method
    val filteredSales = remember(timeFilteredSales, selectedPaymentFilter) {
        timeFilteredSales.filter { sale ->
            selectedPaymentFilter == null || sale.paymentMethod == selectedPaymentFilter
        }
    }

    val chartData = remember(filteredSales, selectedTimeFrame) {
        val bucketCount = when (selectedTimeFrame) {
            CuadreTimeFrame.DIA -> 6
            CuadreTimeFrame.SEMANA -> 6
            CuadreTimeFrame.MES -> 4
        }
        val timeframeMillis = when (selectedTimeFrame) {
            CuadreTimeFrame.DIA -> millisInDay
            CuadreTimeFrame.SEMANA -> 7 * millisInDay
            CuadreTimeFrame.MES -> 30 * millisInDay
        }
        val bucketDuration = timeframeMillis / bucketCount
        val bucketTotals = FloatArray(bucketCount) { 0f }
        for (sale in filteredSales) {
            val diff = now - sale.dateMillis
            if (diff in 0 until timeframeMillis) {
                val bucketIndex = bucketCount - 1 - (diff / bucketDuration).toInt().coerceIn(0, bucketCount - 1)
                bucketTotals[bucketIndex] += sale.totalAmount.toFloat()
            }
        }
        bucketTotals
    }

    // Dynamic calculations derived directly from sales
    val totalRevenue = timeFilteredSales.sumOf { it.totalAmount }
    val cashSales = timeFilteredSales.filter { it.paymentMethod == PaymentMethod.EFECTIVO }.sumOf { it.totalAmount }
    val cardSales = timeFilteredSales.filter { it.paymentMethod == PaymentMethod.TARJETA }.sumOf { it.totalAmount }
    val transferSales = timeFilteredSales.filter { it.paymentMethod == PaymentMethod.TRANSFERENCIA }.sumOf { it.totalAmount }

    // Costo de Ventas value is the sum of filtered sales across payment methods
    val totalCost = filteredSales.flatMap { it.items }.sumOf { it.product.cost * it.quantity }

    val totalProfit = filteredSales.flatMap { it.items }.sumOf { item ->
        (item.product.price - item.product.cost) * item.quantity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3FC))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Cuadre de Caja",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191B22)
            )
            Text(
                text = "Supervisa el rendimiento y flujo de caja",
                fontSize = 12.sp,
                color = Color(0xFF434653)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E2EB).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CuadreTimeFrame.values().forEach { frame ->
                    val isSelected = selectedTimeFrame == frame
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color.White else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { selectedTimeFrame = frame }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = frame.chipLabel,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF191B22) else Color(0xFF737784)
                        )
            }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedTimeFrame.headerTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191B22)
                    )
                    Text(
                        text = selectedTimeFrame.subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF737784)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filtrar",
                    tint = Color(0xFF737784)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Methods Selection Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethodMethodCard(
                        modifier = Modifier.weight(1f),
                        label = "Todos",
                        amount = totalRevenue,
                        icon = Icons.Filled.ReceiptLong,
                        color = Color(0xFF64748B),
                        bgColor = Color(0xFFF8FAFC),
                        isSelected = selectedPaymentFilter == null,
                        onClick = {
                            selectedPaymentFilter = null
                        }
                    )
                    PaymentMethodMethodCard(
                        modifier = Modifier.weight(1f),
                        label = "Efectivo",
                        amount = cashSales,
                        icon = Icons.Filled.Payments,
                        color = Color(0xFF22C55E),
                        bgColor = Color(0xFFF0FDF4),
                        isSelected = selectedPaymentFilter == PaymentMethod.EFECTIVO,
                        onClick = {
                            selectedPaymentFilter = if (selectedPaymentFilter == PaymentMethod.EFECTIVO) null else PaymentMethod.EFECTIVO
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethodMethodCard(
                        modifier = Modifier.weight(1f),
                        label = "Tarjeta",
                        amount = cardSales,
                        icon = Icons.Filled.CreditCard,
                        color = Color(0xFF3B82F6),
                        bgColor = Color(0xFFEFF6FF),
                        isSelected = selectedPaymentFilter == PaymentMethod.TARJETA,
                        onClick = {
                            selectedPaymentFilter = if (selectedPaymentFilter == PaymentMethod.TARJETA) null else PaymentMethod.TARJETA
                        }
                    )
                    PaymentMethodMethodCard(
                        modifier = Modifier.weight(1f),
                        label = "Transf.",
                        amount = transferSales,
                        icon = Icons.Filled.AccountBalance,
                        color = Color(0xFF8B5CF6),
                        bgColor = Color(0xFFF5F3FF),
                        isSelected = selectedPaymentFilter == PaymentMethod.TRANSFERENCIA,
                        onClick = {
                            selectedPaymentFilter = if (selectedPaymentFilter == PaymentMethod.TRANSFERENCIA) null else PaymentMethod.TRANSFERENCIA
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Costo de Ventas
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LuqaSecondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = null,
                                    tint = LuqaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Costo de Ventas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = LuqaOnPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format("S/ %.2f", totalCost),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuqaPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedPaymentFilter == null) "Suma por métodos de pago" else "Filtrado: ${selectedPaymentFilter?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                            fontSize = 11.sp,
                            color = LuqaPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                // Ganancia
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LuqaSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = LuqaSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Ganancia Neta",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF434653)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format("S/ %.2f", totalProfit),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191B22)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val ticketCount = filteredSales.size
            val avgTicket = if (ticketCount > 0) totalRevenue / ticketCount else 0.0
            val avgMargin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100.0 else 0.0
            val topProductEntry = filteredSales.flatMap { it.items }.groupBy { it.product.name }.mapValues { (_, items) -> items.sumOf { it.quantity } }.maxByOrNull { it.value }
            val topProduct = topProductEntry?.key ?: "-"
            val topProductQty = topProductEntry?.value ?: 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ticket Promedio
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ticket Promedio", fontSize = 11.sp, color = Color(0xFF737784))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format("S/ %.2f", avgTicket), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$ticketCount ${if(ticketCount == 1) "ticket" else "tickets"} emitidos", fontSize = 9.sp, color = Color(0xFF737784))
                    }
                }
                // Margen Prom.
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Margen Prom.", fontSize = 11.sp, color = Color(0xFF737784))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format("%.1f %%", avgMargin), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Producto Estrella
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                        Text("Producto Estrella", fontSize = 11.sp, color = Color(0xFF737784))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (topProduct == "-") {
                        Text("Sin ventas en este periodo", fontSize = 13.sp, color = Color(0xFF737784))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(topProduct, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text("$topProductQty uds", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Graph Section: Tendencia de Ventas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Analytics,
                                contentDescription = null,
                                tint = Color(0xFF737784),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tendencia de Ventas",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191B22)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Canvas Graph
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val primaryColor = LuqaPrimary

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height

                            // Grid lines
                            val gridCount = 3
                            for (i in 1..gridCount) {
                                val y = (height / (gridCount + 1)) * i
                                drawLine(
                                    color = Color(0xFFE2E2EB),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }
                            
                            val maxTotal = chartData.maxOrNull() ?: 1f
                            val maxValue = if (maxTotal > 0f) maxTotal else 1f

                            val points = chartData.mapIndexed { index, total ->
                                val x = if (chartData.size > 1) (width / (chartData.size - 1)) * index else width / 2
                                val y = height - (total / maxValue) * (height * 0.7f) - (height * 0.15f)
                                Offset(x, y)
                            }

                            val fillPath = Path().apply {
                                if (points.isNotEmpty()) {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 0 until points.size - 1) {
                                        val current = points[i]
                                        val next = points[i + 1]
                                        val controlX = (current.x + next.x) / 2
                                        cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
                                    }
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                            }

                            val strokePath = Path().apply {
                                if (points.isNotEmpty()) {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 0 until points.size - 1) {
                                        val current = points[i]
                                        val next = points[i + 1]
                                        val controlX = (current.x + next.x) / 2
                                        cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
                                    }
                                }
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.0f)
                                    )
                                )
                            )

                            drawPath(
                                path = strokePath,
                                color = primaryColor,
                                style = Stroke(width = 5f)
                            )

                            for ((index, pt) in points.withIndex()) {
                                drawCircle(color = Color.White, radius = 6f, center = pt)
                                drawCircle(color = primaryColor, radius = 6f, center = pt, style = Stroke(width = 3f))
                                
                                val valueStr = String.format("S/%.1f", chartData[index])
                                val textLayoutResult = textMeasurer.measure(
                                    text = valueStr,
                                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF434653))
                                )
                                val textOffset = Offset(
                                    x = pt.x - (textLayoutResult.size.width / 2f),
                                    y = pt.y - textLayoutResult.size.height - 10f
                                )
                                drawText(textLayoutResult, topLeft = textOffset)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // X-Axis Labels depending on timeframe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        when (selectedTimeFrame) {
                            CuadreTimeFrame.DIA -> {
                                Text("8h", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("11h", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("14h", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("17h", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("20h", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                            }
                            CuadreTimeFrame.SEMANA -> {
                                Text("Lun", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Mar", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Mié", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Jue", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Vie", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                            }
                            CuadreTimeFrame.MES -> {
                                Text("Sem 1", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Sem 2", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Sem 3", fontSize = 11.sp, color = Color(0xFF737784))
                                Text("Sem 4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentMethodMethodCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) color.copy(alpha = 0.2f) else bgColor),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF434653)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("S/ %.1f", amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191B22)
            )
        }
    }
}
