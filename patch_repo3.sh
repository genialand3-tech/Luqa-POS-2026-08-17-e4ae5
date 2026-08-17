#!/bin/bash
sed -i '/fun clearAllSales()/i \
    fun deleteSalesOlderThan(timestamp: Long) {\
        _sales.value = _sales.value.filter { it.dateMillis >= timestamp }\
    }\
\
    fun deleteRefundsOlderThan(timestamp: Long) {\
        _refunds.value = _refunds.value.filter { it.dateMillis >= timestamp }\
    }\
' app/src/main/java/com/example/data/LuqaRepository.kt
