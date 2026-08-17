#!/bin/bash
sed -i '/\/\/ Payment Methods Selection Cards/!b;n;n;n;n;a \
                PaymentMethodMethodCard(\
                    modifier = Modifier.weight(1f),\
                    label = "Todos",\
                    amount = totalRevenue,\
                    icon = Icons.Filled.ReceiptLong,\
                    color = Color(0xFF64748B),\
                    bgColor = Color(0xFFF8FAFC),\
                    isSelected = selectedPaymentFilter == null,\
                    onClick = {\
                        selectedPaymentFilter = null\
                    }\
                )\
' app/src/main/java/com/example/ui/screens/CuadreScreen.kt
