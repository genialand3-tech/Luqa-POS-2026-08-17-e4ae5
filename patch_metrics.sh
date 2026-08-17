#!/bin/bash
sed -i '/\/\/ Graph Section: Tendencia de Ventas/i \
            val ticketCount = filteredSales.size\
            val avgTicket = if (ticketCount > 0) totalRevenue / ticketCount else 0.0\
            val avgMargin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100.0 else 0.0\
            val topProduct = filteredSales.flatMap { it.items }.groupBy { it.product.name }.maxByOrNull { (_, items) -> items.sumOf { it.quantity } }?.key ?: "-"\
\
            Row(\
                modifier = Modifier.fillMaxWidth(),\
                horizontalArrangement = Arrangement.spacedBy(8.dp)\
            ) {\
                // Ticket Promedio\
                Card(\
                    modifier = Modifier.weight(1f),\
                    shape = RoundedCornerShape(12.dp),\
                    colors = CardDefaults.cardColors(containerColor = Color.White),\
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))\
                ) {\
                    Column(modifier = Modifier.padding(12.dp)) {\
                        Text("Ticket Promedio", fontSize = 11.sp, color = Color(0xFF737784))\
                        Spacer(modifier = Modifier.height(4.dp))\
                        Text(String.format("S/ %.2f", avgTicket), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22))\
                    }\
                }\
\
                // Margen Prom.\
                Card(\
                    modifier = Modifier.weight(1f),\
                    shape = RoundedCornerShape(12.dp),\
                    colors = CardDefaults.cardColors(containerColor = Color.White),\
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))\
                ) {\
                    Column(modifier = Modifier.padding(12.dp)) {\
                        Text("Margen Prom.", fontSize = 11.sp, color = Color(0xFF737784))\
                        Spacer(modifier = Modifier.height(4.dp))\
                        Text(String.format("%.1f %%", avgMargin), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22))\
                    }\
                }\
\
                // Producto Estrella\
                Card(\
                    modifier = Modifier.weight(1.2f),\
                    shape = RoundedCornerShape(12.dp),\
                    colors = CardDefaults.cardColors(containerColor = Color.White),\
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))\
                ) {\
                    Column(modifier = Modifier.padding(12.dp)) {\
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {\
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))\
                            Text("Producto Estrella", fontSize = 11.sp, color = Color(0xFF737784))\
                        }\
                        Spacer(modifier = Modifier.height(4.dp))\
                        Text(topProduct, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)\
                    }\
                }\
            }\
\
            Spacer(modifier = Modifier.height(16.dp))\
' app/src/main/java/com/example/ui/screens/CuadreScreen.kt
