import re

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "r") as f:
    content = f.read()

replacement = """            // Time filters
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
            }"""

content = re.sub(r'            // Time filters.*?            }', replacement, content, flags=re.DOTALL, count=1)

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "w") as f:
    f.write(content)

