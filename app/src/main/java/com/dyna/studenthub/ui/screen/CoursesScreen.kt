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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.viewmodel.CoursesViewModel
import com.dyna.studenthub.R
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

private val BasketGreen = Color(0xFF2E7D5B)
private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun CoursesScreen(viewModel: CoursesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showForm by rememberSaveable { mutableStateOf(false) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var formName by rememberSaveable { mutableStateOf<String?>(null) }
    var barcode by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
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
            Text("Courses", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Panier estimé", euros(state.basketTotalCents), Modifier.weight(1f))
                SummaryCard("Budget restant", euros(state.foodBudgetRemainingCents), Modifier.weight(1f))
            }
        }
        item { Text("Produits à acheter", style = MaterialTheme.typography.titleLarge) }
        if (state.products.isEmpty()) {
            item { EmptyState(stringResource(R.string.empty_courses)) }
        }
        items(state.products, key = { it.id }) { product ->
            ProductRow(product, { viewModel.setChecked(product, it) }, { viewModel.deleteProduct(product) })
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
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF3E8)), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Total du panier", style = MaterialTheme.typography.titleMedium)
                    Text(euros(state.basketTotalCents), style = MaterialTheme.typography.headlineSmall, color = BasketGreen)
                }
            }
        }
        item {
            Button(
                onClick = viewModel::finishShopping,
                enabled = state.products.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Terminer les courses") }
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
}

@Composable
private fun SummaryCard(title: String, amount: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(14.dp)) { Text(title); Text(amount, style = MaterialTheme.typography.titleLarge, color = BasketGreen) } }
}

@Composable
private fun ProductRow(product: Product, onChecked: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(product.checked, onChecked)
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge)
            Text("Qté : ${product.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(euros(product.estimatedPriceCents * product.quantity))
        IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, stringResource(R.string.delete_description)) }
    }
}

@Composable
private fun AddProductDialog(initialName: String, barcode: String?, onDismiss: () -> Unit, onAdd: (String, Int, Long) -> Unit) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var price by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val valid = name.isNotBlank() && quantity.toIntOrNull()?.let { it > 0 } == true &&
        price.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un produit") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") }, singleLine = true)
                if (barcode != null) Text("Code-barres : $barcode", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantité") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(price, { price = it }, label = { Text("Prix estimé (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }

        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val quantityValue = quantity.toIntOrNull()
                val priceValue = price.replace(',', '.').toDoubleOrNull()
                if (name.isBlank() || quantityValue == null || quantityValue <= 0 || priceValue == null || priceValue <= 0) {
                    error = true
                } else {
                    onAdd(name, quantityValue, (priceValue * 100).toLong())
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
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Scanner un code-barres") },
        text = {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxWidth().height(280.dp),
                update = { view ->
                    val providerFuture = ProcessCameraProvider.getInstance(context)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
                        ).build()
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
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    }, ContextCompat.getMainExecutor(context))
                },
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCancel) { Text("Annuler") } },
    )
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }
}
