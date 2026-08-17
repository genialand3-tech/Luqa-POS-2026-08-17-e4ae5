#!/bin/bash
sed -i '/Text(topProduct, fontSize = 13.sp, fontWeight = FontWeight.Bold/!b;n;n;n;c \
                        }\
                    }\
                }\
            }\
            Spacer(modifier = Modifier.height(16.dp))\
            \/\/ Graph Section: Tendencia de Ventas\
            Card(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .height(280.dp),\
                shape = RoundedCornerShape(16.dp),\
                colors = CardDefaults.cardColors(containerColor = Color.White),\
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EB))\
            ) {' app/src/main/java/com/example/ui/screens/CuadreScreen.kt
