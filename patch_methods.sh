#!/bin/bash
sed -i '/\/\/ Graph Section/i \
            Text(\
                text = "Ventas por Método de Pago",\
                fontSize = 15.sp,\
                fontWeight = FontWeight.Bold,\
                color = Color(0xFF191B22)\
            )\
            Spacer(modifier = Modifier.height(12.dp))\
\
            Row(\
                modifier = Modifier.fillMaxWidth(),\
                horizontalArrangement = Arrangement.spacedBy(8.dp)\
            ) {\
                val methods = listOf(\
                    PaymentMethod.EFECTIVO to "Efectivo",\
                    PaymentMethod.TARJETA to "Tarjeta",\
                    PaymentMethod.TRANSFERENCIA to "Transf."\
                )\
                methods.forEach { (method, label) ->\
                    val sum = filteredSales.filter { it.paymentMethod == method }.sumOf { it.totalAmount }\
                    Card(\
                        modifier = Modifier.weight(1f),\
                        shape = RoundedCornerShape(12.dp),\
                        colors = CardDefaults.cardColors(containerColor = Color.White),\
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))\
                    ) {\
                        Column(\
                            modifier = Modifier.padding(12.dp),\
                            horizontalAlignment = Alignment.CenterHorizontally\
                        ) {\
                            Text(label, fontSize = 12.sp, color = Color(0xFF737784))\
                            Spacer(modifier = Modifier.height(4.dp))\
                            Text(String.format("S/ %.2f", sum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22))\
                        }\
                    }\
                }\
            }\
\
            Spacer(modifier = Modifier.height(24.dp))\
' app/src/main/java/com/example/ui/screens/CuadreScreen.kt
