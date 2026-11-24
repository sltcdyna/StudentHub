package com.dyna.studenthub.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.viewmodel.CoursesViewModel
import com.dyna.studenthub.domain.GroceryCalculations
import com.dyna.studenthub.domain.Money
import com.dyna.studenthub.R
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun CoursesScreen(viewModel: CoursesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showForm by rememberSaveable { mutableStateOf(false) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }
    var formName by rememberSaveable { mutableStateOf<String?>(null) }
    var barcode by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    val context = LocalContext.current
    val checkedProducts = state.products.filter(Product::checked)
    val checkedTotalCents = GroceryCalculations.totalCents(checkedProducts)
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) showScanner = true
        else {
            message = "Caméra refusée, saisis le produit à la main"
            formName = ""
            showForm = true
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = true,
    ) {
        item {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Mes courses", style = MaterialTheme.typography.headlineMedium)
                Text("Une liste simple, du marché à la cuisine.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Panier estimé", euros(state.basketTotalCents), Modifier.weight(1f), MaterialTheme.colorScheme.primaryContainer)
                SummaryCard("Budget restant", euros(state.foodBudgetRemainingCents), Modifier.weight(1f), MaterialTheme.colorScheme.secondaryContainer)
            }
        }
        item { Text("Produits à acheter", style = MaterialTheme.typography.titleLarge) }
        if (state.products.isEmpty()) {
            item { EmptyState(stringResource(R.string.empty_courses)) }
        }
        items(state.products, key = { it.id }) { product ->
            ProductRow(
                product = product,
                onChecked = { viewModel.setChecked(product, it) },
                onEdit = { productToEdit = product },
                onDelete = { viewModel.deleteProduct(product) },
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { formName = null; barcode = null; showForm = true }) {
                    Text("+ Ajouter un produit")
                }
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    ) showScanner = true else cameraPermission.launch(Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = stringResource(R.string.scan_barcode_description))
                    Text("Scanner")
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Total des articles à prendre", style = MaterialTheme.typography.titleMedium)
                    Text(euros(state.basketTotalCents), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item {
            Button(
                onClick = { showFinishDialog = true },
                enabled = checkedProducts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Valider les achats (${checkedProducts.size})") }
            if (checkedProducts.isEmpty()) {
                Text(
                    "Coche les articles achetés pour les enregistrer dans tes dépenses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Scanner") },
            text = { Text(text) },
            confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } },
        )
    }
    if (showScanner) {
        BarcodeScanner(
            onBarcode = { code ->
                showScanner = false
                barcode = code
                viewModel.lookupBarcode(code) { result ->
                    if (result == null) {
                        message = "Pas de connexion"
                        formName = ""
                    } else if (result.name == null) {
                        message = "Produit inconnu"
                        formName = ""
                    } else formName = result.name
                    showForm = true
                }
            },
            onCancel = { showScanner = false },
        )
    }
    if (showForm) AddProductDialog(
        initialName = formName.orEmpty(),
        barcode = barcode,
        onDismiss = { showForm = false; formName = null; barcode = null },
        onAdd = viewModel::addProduct,
    )
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onSave = { name, quantity, price ->
                viewModel.updateProduct(product, name, quantity, price)
                productToEdit = null
            },
        )
    }
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Valider les achats ?") },
            text = {
                Text(
                    "${checkedProducts.size} article(s) cochés seront enregistrés comme une dépense Alimentation de ${euros(checkedTotalCents)}. Les autres resteront dans ta liste."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    viewModel.finishShopping()
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Continuer les courses") }
            },
        )
    }
}

@Composable
private fun SummaryCard(title: String, amount: String, modifier: Modifier, tint: Color) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = tint)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(amount, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onChecked: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.checked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .56f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(product.checked, onChecked)
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (product.checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (product.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Text("Quantité : ${product.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(euros(product.estimatedPriceCents * product.quantity), style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = "Modifier ${product.name}") }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, stringResource(R.string.delete_description)) }
        }
    }
}

@Composable
private fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (String, Int, Long) -> Unit,
) {
    var name by rememberSaveable(product.id) { mutableStateOf(product.name) }
    var quantity by rememberSaveable(product.id) { mutableStateOf(product.quantity.toString()) }
    var price by rememberSaveable(product.id) {
        mutableStateOf("%.2f".format(Locale.FRANCE, product.estimatedPriceCents / 100.0).replace(',', '.'))
    }
    val priceValue = Money.parseEuroToCents(price)
    val quantityValue = quantity.toIntOrNull()
    val valid = name.isNotBlank() && quantityValue != null && quantityValue > 0 && priceValue != null && priceValue > 0L
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le produit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    quantity,
                    { quantity = it },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    price,
                    { price = it },
                    label = { Text("Prix unitaire estimé (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val amount = priceValue ?: return@TextButton
                val count = quantityValue ?: return@TextButton
                onSave(name, count, amount)
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun AddProductDialog(initialName: String, barcode: String?, onDismiss: () -> Unit, onAdd: (String, Int, Long) -> Unit) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var price by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val priceCents = Money.parseEuroToCents(price)
    val valid = name.isNotBlank() && quantity.toIntOrNull()?.let { it > 0 } == true &&
        priceCents?.let { it > 0L } == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un produit") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (barcode != null) Text("Code-barres : $barcode", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantité") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it }, label = { Text("Prix estimé (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            }

        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val quantityValue = quantity.toIntOrNull()
                val priceValue = Money.parseEuroToCents(price)
                if (name.isBlank() || quantityValue == null || quantityValue <= 0 || priceValue == null || priceValue <= 0) {
                    error = true
                } else {
                    onAdd(name, quantityValue, priceValue)
                    onDismiss()
                }
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Produit invalide") },
            text = { Text(stringResource(R.string.invalid_product)) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
}

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
private fun BarcodeScanner(onBarcode: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var lastCode by remember { mutableStateOf<String?>(null) }
    var lastScanAt by remember { mutableLongStateOf(0L) }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val scanner = BarcodeScanning.getClient()
            analysis.setAnalyzer(executor) { proxy ->
                val image = proxy.image
                if (image == null) {
                    proxy.close()
                    return@setAnalyzer
                }
                scanner.process(InputImage.fromMediaImage(image, proxy.imageInfo.rotationDegrees))
                    .addOnSuccessListener { codes ->
                        val value = codes.firstOrNull()?.rawValue
                        val now = System.currentTimeMillis()
                        if (value != null && (value != lastCode || now - lastScanAt >= 3_000)) {
                            lastCode = value
                            lastScanAt = now
                            onBarcode(value)
                        }
                    }
                    .addOnCompleteListener { proxy.close() }
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) {
                // Ignore camera bind errors if context/lifecycle unmounted
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                providerFuture.get().unbindAll()
            } catch (e: Exception) {
                // Ignore
            }
            executor.shutdown()
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Scanner un code-barres") },
        text = {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxWidth().height(280.dp),
                update = {},
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCancel) { Text("Annuler") } },
    )
}
