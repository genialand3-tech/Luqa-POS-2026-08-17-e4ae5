#!/bin/bash
sed -i '/fun updateProduct/i \
    fun clearAllProducts() {\
        _products.value = emptyList()\
    }\
' app/src/main/java/com/example/data/LuqaRepository.kt
