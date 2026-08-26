package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Dividir parte entera y parte decimal
        val parts = originalText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        
        // Agregar separadores de miles a la parte entera
        val formattedInteger = if (integerPart.isNotEmpty()) {
            val symbols = DecimalFormatSymbols(Locale.US)
            symbols.groupingSeparator = ','
            val formatter = DecimalFormat("#,###", symbols)
            try {
                formatter.format(integerPart.toLong())
            } catch (e: Exception) {
                integerPart
            }
        } else {
            ""
        }

        val formattedText = formattedInteger + decimalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                
                var transformedOffset = 0
                var originalOffset = 0
                
                while (originalOffset < offset && transformedOffset < formattedText.length) {
                    if (formattedText[transformedOffset] == ',') {
                        transformedOffset++
                    } else {
                        originalOffset++
                        transformedOffset++
                    }
                }
                
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                
                var transformedOffset = 0
                var originalOffset = 0
                
                while (transformedOffset < offset && originalOffset < originalText.length) {
                    if (formattedText[transformedOffset] == ',') {
                        transformedOffset++
                    } else {
                        originalOffset++
                        transformedOffset++
                    }
                }
                
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
