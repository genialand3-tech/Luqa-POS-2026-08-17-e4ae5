package com.example.data

import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.RefundRecord
import com.example.model.SaleRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.tasks.await

class LuqaRepository {
    private val db = FirebaseFirestore.getInstance()
    private var tenantId: String? = null

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleRecord>>(emptyList())
    val sales: StateFlow<List<SaleRecord>> = _sales.asStateFlow()

    private val _refunds = MutableStateFlow<List<RefundRecord>>(emptyList())
    val refunds: StateFlow<List<RefundRecord>> = _refunds.asStateFlow()

    fun initialize(uid: String) {
        if (tenantId == uid) return
        tenantId = uid
        startListeners(uid)
    }

    private fun startListeners(uid: String) {
        // Products
        db.collection("users").document(uid).collection("products")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                    _products.value = list
                }
            }

        // Sales
        db.collection("users").document(uid).collection("sales")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(SaleRecord::class.java) }
                    _sales.value = list
                }
            }

        // Refunds
        db.collection("users").document(uid).collection("refunds")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(RefundRecord::class.java) }
                    _refunds.value = list
                }
            }

        // Categories
        db.collection("users").document(uid).collection("categories")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.getString("name") }
                    _categories.value = list
                }
            }
    }

    suspend fun exportSalesHistory(context: android.content.Context): android.net.Uri? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(context.cacheDir, "historial_ventas_${System.currentTimeMillis()}.csv")
                file.printWriter().use { out ->
                    out.println("ID Venta,Numero Operacion,Fecha/Hora,Metodo Pago,Total,ID Producto,Nombre Producto,Cantidad,Precio Unitario,Subtotal")
                    _sales.value.forEach { sale ->
                        sale.items.forEach { item ->
                            out.println("${sale.id},${sale.operationNumber},\"${sale.timestamp}\",${sale.paymentMethod.label},${sale.totalAmount},${item.product.id},\"${item.product.name}\",${item.quantity},${item.product.price},${item.totalPrice}")
                        }
                    }
                }
                androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun exportInventoryToCsv(context: android.content.Context): android.net.Uri? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(context.cacheDir, "inventario_${System.currentTimeMillis()}.csv")
                file.printWriter().use { out ->
                    out.println("Nombre,Categoria,Costo,Precio,Stock,SKU,CodigoBarras")
                    _products.value.forEach { product ->
                        out.println("${product.name},${product.category},${product.cost},${product.price},${product.stock},${product.sku},${product.barcode}")
                    }
                }
                androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // CART METHODS (Local only)
    fun addProductToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cart.value = current
    }

    fun decrementCartProduct(productId: String) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity > 1) {
                current[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.removeAt(index)
            }
            _cart.value = current
        }
    }

    fun removeCartItem(productId: String) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun addComboToCart(comboName: String) {
        when (comboName) {
            "Combo Desayuno" -> {
                val cafe = _products.value.firstOrNull { it.name == "Café Pasado" }
                val arepa = _products.value.firstOrNull { it.name == "Arepa de Queso" }
                if (cafe != null) addProductToCart(cafe)
                if (arepa != null) addProductToCart(arepa)
            }
            "Promo Arepas" -> {
                val arepa = _products.value.firstOrNull { it.name == "Arepa de Queso" }
                if (arepa != null) {
                    addProductToCart(arepa)
                    addProductToCart(arepa)
                }
            }
        }
    }

    // FIRESTORE MUTATIONS
    fun createNewProduct(newProduct: Product) {
        val tid = tenantId ?: return
        db.collection("users").document(tid).collection("products")
            .document(newProduct.id).set(newProduct)
            
        if (newProduct.category.isNotBlank()) {
            addCategory(newProduct.category)
        }
    }

    fun updateProduct(updatedProduct: Product) {
        val tid = tenantId ?: return
        db.collection("users").document(tid).collection("products")
            .document(updatedProduct.id).set(updatedProduct)
            
        if (updatedProduct.category.isNotBlank()) {
            addCategory(updatedProduct.category)
        }
    }

    fun deleteObsoleteProducts(option: String) {
        val tid = tenantId ?: return
        val batch = db.batch()
        val toDelete = _products.value.filter { product ->
            when (option) {
                "inactive" -> !product.isActive
                "out_of_stock" -> product.stock <= 0
                "both" -> !product.isActive || product.stock <= 0
                else -> false
            }
        }
        toDelete.forEach { p ->
            val ref = db.collection("users").document(tid).collection("products").document(p.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun clearAllProducts() {
        val tid = tenantId ?: return
        val batch = db.batch()
        _products.value.forEach { p ->
            val ref = db.collection("users").document(tid).collection("products").document(p.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun checkout(paymentMethod: PaymentMethod): SaleRecord {
        val tid = tenantId ?: throw IllegalStateException("Tenant ID no está inicializado")
        val cartItems = _cart.value
        val total = cartItems.sumOf { it.totalPrice }
        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
        val randomOp = "TRX-" + (100000..999999).random() + "A"
        
        val sale = SaleRecord(
            id = UUID.randomUUID().toString(),
            operationNumber = randomOp,
            timestamp = dateFormat.format(Date()),
            totalAmount = total,
            paymentMethod = paymentMethod,
            items = cartItems,
            dateMillis = System.currentTimeMillis()
        )

        db.collection("users").document(tid).collection("sales").document(sale.id).set(sale)

        val batch = db.batch()
        cartItems.forEach { item ->
            val p = _products.value.find { it.id == item.product.id }
            if (p != null) {
                val newStock = (p.stock - item.quantity).coerceAtLeast(0)
                val ref = db.collection("users").document(tid).collection("products").document(p.id)
                batch.update(ref, "stock", newStock)
            }
        }
        batch.commit()
        return sale
    }

    fun processRefund(
        saleId: String,
        reason: String,
        returnedItemsOverride: List<CartItem>? = null
    ): RefundRecord? {
        val tid = tenantId ?: return null
        val sale = _sales.value.find { it.id == saleId } ?: return null
        val itemsToReturn = returnedItemsOverride ?: sale.items
        if (itemsToReturn.isEmpty()) return null

        val batch = db.batch()

        itemsToReturn.forEach { returned ->
            if (returned.quantity > 0) {
                val p = _products.value.find { it.id == returned.product.id }
                if (p != null) {
                    val ref = db.collection("users").document(tid).collection("products").document(p.id)
                    batch.update(ref, "stock", p.stock + returned.quantity)
                }
            }
        }

        val totalRefunded = itemsToReturn.sumOf { it.totalPrice }
        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
        val refundRecord = RefundRecord(
            id = UUID.randomUUID().toString(),
            saleId = sale.id,
            operationNumber = sale.operationNumber,
            timestamp = "Hoy, " + dateFormat.format(Date()),
            reason = reason.ifBlank { "Devolución registrada por cliente" },
            returnedItems = itemsToReturn,
            totalRefunded = totalRefunded,
            paymentMethod = sale.paymentMethod,
            dateMillis = System.currentTimeMillis()
        )
        val refundRef = db.collection("users").document(tid).collection("refunds").document(refundRecord.id)
        batch.set(refundRef, refundRecord)

        val saleRef = db.collection("users").document(tid).collection("sales").document(sale.id)
        if (returnedItemsOverride == null || (returnedItemsOverride.size == sale.items.size && returnedItemsOverride.containsAll(sale.items))) {
            batch.delete(saleRef)
        } else {
            val remainingItems = sale.items.mapNotNull { orig ->
                val retQty = itemsToReturn.find { it.product.id == orig.product.id }?.quantity ?: 0
                val remQty = orig.quantity - retQty
                if (remQty > 0) orig.copy(quantity = remQty) else null
            }

            if (remainingItems.isEmpty()) {
                batch.delete(saleRef)
            } else {
                val remainingTotal = remainingItems.sumOf { it.totalPrice }
                batch.set(saleRef, sale.copy(items = remainingItems, totalAmount = remainingTotal))
            }
        }
        
        batch.commit()
        return refundRecord
    }

    fun updateSaleItems(saleId: String, newItems: List<CartItem>, reason: String = "Modificación de ticket / Ajuste de items") {
        val tid = tenantId ?: return
        val existingSale = _sales.value.find { it.id == saleId } ?: return

        if (newItems.isEmpty()) {
            processRefund(saleId, reason)
            return
        }

        val returnedItems = mutableListOf<CartItem>()
        val batch = db.batch()

        _products.value.forEach { p ->
            val oldQty = existingSale.items.find { it.product.id == p.id }?.quantity ?: 0
            val newQty = newItems.find { it.product.id == p.id }?.quantity ?: 0
            val diff = oldQty - newQty
            
            if (diff != 0) {
                val newStock = (p.stock + diff).coerceAtLeast(0)
                val ref = db.collection("users").document(tid).collection("products").document(p.id)
                batch.update(ref, "stock", newStock)
                if (diff > 0) {
                    returnedItems.add(CartItem(p, diff))
                }
            }
        }

        if (returnedItems.isNotEmpty()) {
            val totalRefunded = returnedItems.sumOf { it.totalPrice }
            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
            val refundRecord = RefundRecord(
                id = UUID.randomUUID().toString(),
                saleId = existingSale.id,
                operationNumber = existingSale.operationNumber,
                timestamp = "Hoy, " + dateFormat.format(Date()),
                reason = reason,
                returnedItems = returnedItems,
                totalRefunded = totalRefunded,
                paymentMethod = existingSale.paymentMethod,
                dateMillis = System.currentTimeMillis()
            )
            val refundRef = db.collection("users").document(tid).collection("refunds").document(refundRecord.id)
            batch.set(refundRef, refundRecord)
        }

        val newTotal = newItems.sumOf { it.totalPrice }
        val saleRef = db.collection("users").document(tid).collection("sales").document(existingSale.id)
        batch.set(saleRef, existingSale.copy(items = newItems, totalAmount = newTotal))
        
        batch.commit()
    }

    fun deleteSalesOlderThan(timestamp: Long) {
        val tid = tenantId ?: return
        val batch = db.batch()
        _sales.value.filter { it.dateMillis < timestamp }.forEach { s ->
            val ref = db.collection("users").document(tid).collection("sales").document(s.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun deleteRefundsOlderThan(timestamp: Long) {
        val tid = tenantId ?: return
        val batch = db.batch()
        _refunds.value.filter { it.dateMillis < timestamp }.forEach { r ->
            val ref = db.collection("users").document(tid).collection("refunds").document(r.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun clearAllSales() {
        val tid = tenantId ?: return
        val batch = db.batch()
        _sales.value.forEach { s ->
            val ref = db.collection("users").document(tid).collection("sales").document(s.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun clearAllRefunds() {
        val tid = tenantId ?: return
        val batch = db.batch()
        _refunds.value.forEach { r ->
            val ref = db.collection("users").document(tid).collection("refunds").document(r.id)
            batch.delete(ref)
        }
        batch.commit()
    }

    fun deleteSale(saleId: String) {
        processRefund(saleId, "Anulación completa de ticket / Devolución")
    }

    fun addCategory(categoryName: String) {
        val tid = tenantId ?: return
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return
        
        val docData = hashMapOf("name" to trimmed)
        db.collection("users").document(tid).collection("categories")
            .document(trimmed).set(docData, SetOptions.merge())
    }

    fun renameCategory(oldName: String, newName: String) {
        val tid = tenantId ?: return
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || oldName == trimmedNew) return
        
        val batch = db.batch()
        
        val oldRef = db.collection("users").document(tid).collection("categories").document(oldName)
        val newRef = db.collection("users").document(tid).collection("categories").document(trimmedNew)
        batch.delete(oldRef)
        batch.set(newRef, hashMapOf("name" to trimmedNew))

        _products.value.forEach { p ->
            if (p.category.equals(oldName, ignoreCase = true)) {
                val pRef = db.collection("users").document(tid).collection("products").document(p.id)
                batch.update(pRef, "category", trimmedNew)
            }
        }
        batch.commit()
    }

    fun deleteCategory(categoryName: String) {
        val tid = tenantId ?: return
        val batch = db.batch()

        val ref = db.collection("users").document(tid).collection("categories").document(categoryName)
        batch.delete(ref)

        val fallbackCategory = "General"
        val fallbackRef = db.collection("users").document(tid).collection("categories").document(fallbackCategory)
        batch.set(fallbackRef, hashMapOf("name" to fallbackCategory), SetOptions.merge())

        _products.value.forEach { p ->
            if (p.category.equals(categoryName, ignoreCase = true)) {
                val pRef = db.collection("users").document(tid).collection("products").document(p.id)
                batch.update(pRef, "category", fallbackCategory)
            }
        }
        batch.commit()
    }

    suspend fun importCsv(context: android.content.Context, uri: android.net.Uri): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val tid = tenantId ?: return@withContext "Error: No autenticado"
            try {
                var addedCount = 0
                var updatedCount = 0
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = inputStream?.bufferedReader()
                reader?.useLines { lines ->
                    val linesList = lines.toList()
                    if (linesList.size <= 1) return@withContext "Error: El archivo CSV está vacío o solo contiene encabezados."
                    
                    for (i in 1 until linesList.size) {
                        val line = linesList[i]
                        if (line.isBlank()) continue
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size < 5) {
                            return@withContext "Error en línea ${i+1}: Faltan columnas."
                        }
                        if (parts[0].isBlank()) {
                            return@withContext "Error en línea ${i+1}: El Nombre no puede estar vacío."
                        }
                        if (parts[2].toDoubleOrNull() == null) {
                            return@withContext "Error en línea ${i+1}: El Costo debe ser numérico."
                        }
                        if (parts[3].toDoubleOrNull() == null) {
                            return@withContext "Error en línea ${i+1}: El Precio debe ser numérico."
                        }
                        if (parts[4].toIntOrNull() == null) {
                            return@withContext "Error en línea ${i+1}: El Stock debe ser un número entero."
                        }
                    }

                    val batch = db.batch()
                    linesList.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val parts = line.split(",").map { it.trim() }
                        val name = parts[0]
                        val category = parts[1].ifBlank { "General" }
                        val cost = parts[2].toDoubleOrNull() ?: 0.0
                        val price = parts[3].toDoubleOrNull() ?: 0.0
                        val stock = parts[4].toIntOrNull() ?: 0
                        val sku = if (parts.size > 5) parts[5] else ""
                        val barcode = if (parts.size > 6) parts[6] else ""
                        val imageUrl = if (parts.size > 7 && parts[7].isNotBlank()) parts[7] else null

                        val existing = _products.value.find {
                            (sku.isNotBlank() && it.sku == sku) ||
                            (barcode.isNotBlank() && it.barcode == barcode) ||
                            (it.name.equals(name, ignoreCase = true))
                        }

                        if (existing != null) {
                            val updated = existing.copy(
                                stock = existing.stock + stock,
                                cost = if (cost > 0) cost else existing.cost,
                                price = if (price > 0) price else existing.price,
                                category = category,
                                imageUrl = imageUrl ?: existing.imageUrl,
                                isActive = true
                            )
                            val ref = db.collection("users").document(tid).collection("products").document(existing.id)
                            batch.set(ref, updated)
                            updatedCount++
                        } else {
                            val newId = UUID.randomUUID().toString()
                            val newProduct = Product(
                                id = newId,
                                name = name,
                                category = category,
                                cost = cost,
                                price = price,
                                stock = stock,
                                sku = sku,
                                barcode = barcode,
                                imageUrl = imageUrl,
                                isActive = true
                            )
                            val ref = db.collection("users").document(tid).collection("products").document(newId)
                            batch.set(ref, newProduct)
                            addedCount++
                            
                            val catRef = db.collection("users").document(tid).collection("categories").document(category)
                            batch.set(catRef, hashMapOf("name" to category), SetOptions.merge())
                        }
                    }
                    batch.commit().await()
                }
                "Importación exitosa: $addedCount agregados, $updatedCount actualizados."
            } catch (e: Exception) {
                "Error al importar CSV: ${e.message}"
            }
        }
    }
}
