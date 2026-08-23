package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LuqaRepository
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.RefundRecord
import com.example.model.SaleRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavDestination {
    VENTAS, HISTORIAL, CUADRE, INVENTARIO
}

class LuqaViewModel(
    private val repository: LuqaRepository = LuqaRepository()
) : ViewModel() {

    fun initializeWithTenant(uid: String) {
        repository.initialize(uid)
    }

    private val _currentNav = MutableStateFlow(NavDestination.VENTAS)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lastSale = MutableStateFlow<SaleRecord?>(null)
    val lastSale: StateFlow<SaleRecord?> = _lastSale.asStateFlow()

    private val _showSuccessModal = MutableStateFlow(false)
    val showSuccessModal: StateFlow<Boolean> = _showSuccessModal.asStateFlow()

    private val _showReceiptModal = MutableStateFlow(false)
    val showReceiptModal: StateFlow<Boolean> = _showReceiptModal.asStateFlow()

    private val _showAddProductModal = MutableStateFlow(false)
    val showAddProductModal: StateFlow<Boolean> = _showAddProductModal.asStateFlow()

    private val _showManageCategoriesModal = MutableStateFlow(false)
    val showManageCategoriesModal: StateFlow<Boolean> = _showManageCategoriesModal.asStateFlow()

    private val _productToEdit = MutableStateFlow<Product?>(null)
    val productToEdit: StateFlow<Product?> = _productToEdit.asStateFlow()

    val categories: StateFlow<List<String>> = repository.categories
    val products: StateFlow<List<Product>> = repository.products
    val cart: StateFlow<List<CartItem>> = repository.cart
    val sales: StateFlow<List<SaleRecord>> = repository.sales
    val refunds: StateFlow<List<RefundRecord>> = repository.refunds


    fun exportSalesHistory(context: android.content.Context, onResult: (android.net.Uri?) -> Unit) {
        viewModelScope.launch {
            val uri = repository.exportSalesHistory(context)
            onResult(uri)
        }
    }

    val lowStockProducts: StateFlow<List<Product>> = combine(products) { all ->
        all[0].filter { it.isLowStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val outOfStockProducts: StateFlow<List<Product>> = combine(products) { all ->
        all[0].filter { it.isOutOfStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val criticalStockCount: StateFlow<Int> = combine(lowStockProducts, outOfStockProducts) { low, out ->
        low.size + out.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val categoriesWithAll: StateFlow<List<String>> = combine(
        categories,
        lowStockProducts,
        outOfStockProducts
    ) { cats, low, out ->
        val list = mutableListOf("Todas")
        if (low.isNotEmpty()) {
            list.add("⚠️ Bajo Stock (${low.size})")
        }
        if (out.isNotEmpty()) {
            list.add("🚫 Agotados (${out.size})")
        }
        list.addAll(cats)
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Todas")
    )

    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        selectedCategory,
        searchQuery
    ) { allProducts, category, query ->
        allProducts.filter { p ->
            val matchesCategory = when {
                category == "Todas" -> true
                category.startsWith("⚠️ Bajo Stock") -> p.isLowStock
                category.startsWith("🚫 Agotados") -> p.isOutOfStock
                else -> p.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(destination: NavDestination) {
        _currentNav.value = destination
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product) {
        repository.addProductToCart(product)
    }

    fun decrementCartProduct(productId: String) {
        repository.decrementCartProduct(productId)
    }

    fun removeCartItem(productId: String) {
        repository.removeCartItem(productId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    fun addComboToCart(comboName: String) {
        repository.addComboToCart(comboName)
    }

    fun processPayment(paymentMethod: PaymentMethod) {
        if (cart.value.isEmpty()) return
        val sale = repository.checkout(paymentMethod)
        _lastSale.value = sale
        _showSuccessModal.value = true
    }

    fun dismissSuccessModal() {
        _showSuccessModal.value = false
        repository.clearCart()
        _currentNav.value = NavDestination.VENTAS
    }

    fun openReceiptModal() {
        _showSuccessModal.value = false
        _showReceiptModal.value = true
    }

    fun dismissReceiptModal() {
        _showReceiptModal.value = false
        repository.clearCart()
        _currentNav.value = NavDestination.VENTAS
    }

    fun openAddProductModal() {
        _showAddProductModal.value = true
    }

    fun dismissAddProductModal() {
        _showAddProductModal.value = false
    }

    fun openEditProductModal(product: Product) {
        _productToEdit.value = product
    }

    fun dismissEditProductModal() {
        _productToEdit.value = null
    }

    fun updateProduct(product: Product) {
        repository.updateProduct(product)
        _productToEdit.value = null
    }

    fun saveProduct(
        name: String,
        category: String,
        cost: Double,
        price: Double,
        stock: Int,
        sku: String,
        barcode: String,
        imageUrl: String? = null
    ) {
        val colors = listOf(0xFFE2E2EB, 0xFFFFD7D7, 0xFFD7FFD9, 0xFFFFF8D7, 0xFFE2D7FF)
        val newProduct = Product(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            category = category,
            cost = cost,
            price = price,
            stock = stock,
            sku = sku,
            barcode = barcode,
            imageUrl = imageUrl,
            placeholderText = name.take(2).uppercase(),
            placeholderColorHex = colors.random()
        )
        viewModelScope.launch {
            repository.createNewProduct(newProduct)
        }
        _showAddProductModal.value = false
    }

    fun exportInventoryToCsv(context: android.content.Context, onResult: (android.net.Uri?) -> Unit) {
        viewModelScope.launch {
            val uri = repository.exportInventoryToCsv(context)
            onResult(uri)
        }
    }

    fun importProductsFromCsv(context: android.content.Context, uri: android.net.Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importCsv(context, uri)
            onResult(result)
        }
    }

    fun openManageCategoriesModal() {
        _showManageCategoriesModal.value = true
    }

    fun dismissManageCategoriesModal() {
        _showManageCategoriesModal.value = false
    }

    fun addCategory(categoryName: String) {
        repository.addCategory(categoryName)
    }

    fun renameCategory(oldName: String, newName: String) {
        repository.renameCategory(oldName, newName)
        if (_selectedCategory.value.equals(oldName, ignoreCase = true)) {
            _selectedCategory.value = newName
        }
    }

    fun deleteCategory(categoryName: String) {
        repository.deleteCategory(categoryName)
        if (_selectedCategory.value.equals(categoryName, ignoreCase = true)) {
            _selectedCategory.value = "Todas"
        }
    }

    fun deleteSalesOlderThan(timestamp: Long) {
        repository.deleteSalesOlderThan(timestamp)
    }

    fun deleteRefundsOlderThan(timestamp: Long) {
        repository.deleteRefundsOlderThan(timestamp)
    }

    fun clearAllSales() {
        repository.clearAllSales()
    }

    fun clearAllRefunds() {
        repository.clearAllRefunds()
    }

    fun deleteObsoleteProducts(option: String) {
        repository.deleteObsoleteProducts(option)
    }

    fun clearAllProducts() {
        repository.clearAllProducts()
    }

    fun deleteSale(saleId: String) {
        repository.deleteSale(saleId)
    }

    fun processRefund(
        saleId: String,
        reason: String,
        itemsReturned: List<CartItem>? = null
    ) {
        repository.processRefund(saleId, reason, itemsReturned)
    }

    fun updateSaleItems(saleId: String, updatedItems: List<CartItem>, reason: String = "Ajuste / Devolución parcial de ticket") {
        repository.updateSaleItems(saleId, updatedItems, reason)
    }
}
