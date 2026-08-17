import re

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "r") as f:
    content = f.read()

replacement = """                            Text(topProduct, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191B22), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text("$topProductQty uds", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuqaPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Graph Section: Tendencia de Ventas
            Card("""

content = re.sub(r'                            Text\(topProduct,.*?            } \{', replacement + ' {\n', content, flags=re.DOTALL, count=1)

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "w") as f:
    f.write(content)

