import re

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "r") as f:
    content = f.read()

replacement = """            Row(
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
            }"""

content = re.sub(r'            Row\(\n                modifier = Modifier\.fillMaxWidth\(\),\n                horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\n            \) {\n                // Ticket Promedio.*?            }\n', replacement + '\n', content, flags=re.DOTALL, count=1)

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "w") as f:
    f.write(content)

