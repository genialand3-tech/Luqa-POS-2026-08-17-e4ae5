package com.example.model

enum class PaymentMethod(val label: String, val badgeText: String) {
    EFECTIVO("Efectivo", "Efectivo Recibido"),
    TARJETA("Tarjeta", "Tarjeta Aprobada"),
    TRANSFERENCIA("Transf.", "Transferencia Recibida")
}

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val cost: Double = 0.0,
    val price: Double = 0.0,
    val stock: Int = 0,
    val sku: String = "",
    val barcode: String = "",
    val imageUrl: String? = null,
    val placeholderText: String? = null,
    val placeholderColorHex: Long = 0xFFE2E2EB,
    val isActive: Boolean = true,
    val minStockThreshold: Int = 5
) {
    val marginPercentage: Double
        get() = if (price > 0) ((price - cost) / price) * 100.0 else 0.0

    val totalInventoryValue: Double
        get() = stock * price

    val isOutOfStock: Boolean
        get() = isActive && stock <= 0

    val isLowStock: Boolean
        get() = isActive && stock in 1..minStockThreshold
}

data class CartItem(
    val product: Product = Product(),
    val quantity: Int = 0
) {
    val totalPrice: Double
        get() = product.price * quantity
}

data class Combo(
    val id: String = "",
    val name: String = "",
    val items: List<CartItem> = emptyList()
)

data class SaleRecord(
    val id: String = "",
    val operationNumber: String = "",
    val timestamp: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val items: List<CartItem> = emptyList(),
    val dateMillis: Long = System.currentTimeMillis()
)

data class RefundRecord(
    val id: String = "",
    val saleId: String = "",
    val operationNumber: String = "",
    val timestamp: String = "",
    val reason: String = "",
    val returnedItems: List<CartItem> = emptyList(),
    val totalRefunded: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val dateMillis: Long = System.currentTimeMillis()
)
