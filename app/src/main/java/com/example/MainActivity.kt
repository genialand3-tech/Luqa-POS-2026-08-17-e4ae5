package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AddProductModal
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.EditProductModal
import com.example.ui.components.ManageCategoriesModal
import com.example.ui.components.ReceiptModal
import com.example.ui.components.SuccessModal
import com.example.ui.screens.CuadreScreen
import com.example.ui.screens.HistorialScreen
import com.example.ui.screens.InventarioScreen
import com.example.ui.screens.VentasScreen
import com.example.ui.theme.LuqaTheme
import com.example.viewmodel.LuqaViewModel
import com.example.viewmodel.NavDestination

import com.google.firebase.auth.FirebaseAuth
import com.example.ui.screens.LoginScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout

class MainActivity : ComponentActivity() {

    private val viewModel: LuqaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuqaTheme {
                var currentUid by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid) }

                if (currentUid == null) {
                    LoginScreen(onLoginSuccess = { uid ->
                        currentUid = uid
                        viewModel.initializeWithTenant(uid)
                    })
                } else {
                    LaunchedEffect(currentUid) {
                        viewModel.initializeWithTenant(currentUid!!)
                    }
                    LuqaApp(
                        viewModel = viewModel,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            currentUid = null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LuqaApp(viewModel: LuqaViewModel, onLogout: () -> Unit) {
    val currentNav by viewModel.currentNav.collectAsState()
    val showSuccessModal by viewModel.showSuccessModal.collectAsState()
    val showReceiptModal by viewModel.showReceiptModal.collectAsState()
    val showAddProductModal by viewModel.showAddProductModal.collectAsState()
    val showManageCategoriesModal by viewModel.showManageCategoriesModal.collectAsState()
    val productToEdit by viewModel.productToEdit.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val lastSale by viewModel.lastSale.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Luqa POS") },
                actions = {
                    androidx.compose.material3.IconButton(onClick = onLogout) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentNav = currentNav,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Content
            Crossfade(
                targetState = currentNav,
                modifier = Modifier.fillMaxSize(),
                label = "screen_transition"
            ) { destination ->
                when (destination) {
                    NavDestination.VENTAS -> VentasScreen(viewModel = viewModel)
                    NavDestination.HISTORIAL -> HistorialScreen(viewModel = viewModel)
                    NavDestination.CUADRE -> CuadreScreen(viewModel = viewModel)
                    NavDestination.INVENTARIO -> InventarioScreen(viewModel = viewModel)
                }
            }
        }

        // Dialog Modals
        if (showSuccessModal) {
            SuccessModal(
                sale = lastSale,
                onDismiss = { viewModel.dismissSuccessModal() },
                onViewReceipt = { viewModel.openReceiptModal() }
            )
        }

        if (showReceiptModal) {
            ReceiptModal(
                sale = lastSale,
                onDismiss = { viewModel.dismissReceiptModal() }
            )
        }

        if (showAddProductModal) {
            AddProductModal(
                availableCategories = categories,
                onDismiss = { viewModel.dismissAddProductModal() },
                onSave = { name, category, cost, price, stock, sku, barcode, imageUrl ->
                    viewModel.saveProduct(name, category, cost, price, stock, sku, barcode, imageUrl)
                }
            )
        }

        productToEdit?.let { product ->
            EditProductModal(
                product = product,
                availableCategories = categories,
                onDismiss = { viewModel.dismissEditProductModal() },
                onSave = { updatedProduct ->
                    viewModel.updateProduct(updatedProduct)
                }
            )
        }

        if (showManageCategoriesModal) {
            ManageCategoriesModal(
                categories = categories,
                onDismiss = { viewModel.dismissManageCategoriesModal() },
                onAddCategory = { viewModel.addCategory(it) },
                onRenameCategory = { oldName, newName -> viewModel.renameCategory(oldName, newName) },
                onDeleteCategory = { viewModel.deleteCategory(it) }
            )
        }
    }
}

