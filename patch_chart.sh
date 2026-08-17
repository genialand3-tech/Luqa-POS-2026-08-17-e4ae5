#!/bin/bash
sed -i '/val filteredSales = remember(timeFilteredSales, selectedPaymentFilter) {/,/    }/a \
    val chartData = remember(filteredSales, selectedTimeFrame) {\
        val bucketCount = when (selectedTimeFrame) {\
            CuadreTimeFrame.DIA -> 6\
            CuadreTimeFrame.SEMANA -> 6\
            CuadreTimeFrame.MES -> 4\
        }\
        val timeframeMillis = when (selectedTimeFrame) {\
            CuadreTimeFrame.DIA -> millisInDay\
            CuadreTimeFrame.SEMANA -> 7 * millisInDay\
            CuadreTimeFrame.MES -> 30 * millisInDay\
        }\
        val bucketDuration = timeframeMillis / bucketCount\
        val bucketTotals = FloatArray(bucketCount) { 0f }\
        for (sale in filteredSales) {\
            val diff = now - sale.dateMillis\
            if (diff in 0 until timeframeMillis) {\
                val bucketIndex = bucketCount - 1 - (diff / bucketDuration).toInt().coerceIn(0, bucketCount - 1)\
                bucketTotals[bucketIndex] += sale.totalAmount.toFloat()\
            }\
        }\
        bucketTotals\
    }' app/src/main/java/com/example/ui/screens/CuadreScreen.kt
