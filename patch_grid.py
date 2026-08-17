import re

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "r") as f:
    content = f.read()

replacement = """            // Payment Methods Selection Cards
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
            }"""

content = re.sub(r'            // Payment Methods Selection Cards.*?            }', replacement, content, flags=re.DOTALL, count=1)

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "w") as f:
    f.write(content)

