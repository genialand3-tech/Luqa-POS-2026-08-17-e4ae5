#!/bin/bash
sed -i '/import androidx.compose.material.icons.filled.Search/a import androidx.compose.material.icons.filled.Send' app/src/main/java/com/example/ui/screens/InventarioScreen.kt

sed -i '/IconButton(/i \
                IconButton(\
                    onClick = {\
                        viewModel.exportInventoryToCsv(context) { uri ->\
                            if (uri != null) {\
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {\
                                    type = "text/csv"\
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)\
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)\
                                }\
                                context.startActivity(android.content.Intent.createChooser(intent, "Exportar Inventario"))\
                            } else {\
                                Toast.makeText(context, "Error al exportar inventario", Toast.LENGTH_SHORT).show()\
                            }\
                        }\
                    },\
                    modifier = Modifier\
                        .size(42.dp)\
                        .clip(RoundedCornerShape(10.dp))\
                        .background(LuqaPrimary.copy(alpha = 0.1f)),\
                    colors = IconButtonDefaults.iconButtonColors(contentColor = LuqaPrimary)\
                ) {\
                    Icon(\
                        imageVector = Icons.Filled.Send,\
                        contentDescription = "Exportar CSV"\
                    )\
                }\
\
                Spacer(modifier = Modifier.width(8.dp))\
' app/src/main/java/com/example/ui/screens/InventarioScreen.kt
