import re

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "r") as f:
    content = f.read()

replacement = """                            val fillPath = Path().apply {
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
                            }"""

content = re.sub(r'                            val fillPath = Path\(\)\.apply \{.*?                            val strokePath = Path\(\)\.apply \{.*?\n                            \}', replacement, content, flags=re.DOTALL, count=1)

with open("app/src/main/java/com/example/ui/screens/CuadreScreen.kt", "w") as f:
    f.write(content)

