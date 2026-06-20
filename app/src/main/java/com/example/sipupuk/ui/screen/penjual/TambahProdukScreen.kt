package com.example.sipupuk.ui.screen.penjual

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sipupuk.data.model.KategoriProduk
import com.example.sipupuk.data.model.Satuan
import com.example.sipupuk.ui.viewmodel.ProdukViewModel
import com.example.sipupuk.ui.viewmodel.ProdukViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ✅ Visual Transformation untuk format ribuan
class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Format dengan titik ribuan
        val formatted = try {
            val number = originalText.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
            val symbols = DecimalFormatSymbols(Locale("id", "ID"))
            symbols.groupingSeparator = '.'
            val formatter = DecimalFormat("#,###", symbols)
            formatter.format(number)
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0
                val dotsBeforeOffset = originalText.take(offset).chunked(3).size - 1
                return offset + dotsBeforeOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                val dots = formatted.take(offset).count { it == '.' }
                return (offset - dots).coerceAtLeast(0)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(
    produkId: Int? = null,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val produkViewModel: ProdukViewModel = viewModel(factory = ProdukViewModelFactory(context))

    val createProdukState by produkViewModel.createProdukState.collectAsState()
    val updateProdukState by produkViewModel.updateProdukState.collectAsState()
    val kategoriListState by produkViewModel.kategoriListState.collectAsState()
    val satuanListState by produkViewModel.satuanListState.collectAsState()
    val produkList by produkViewModel.produkList.collectAsState()

    var namaProduk by remember { mutableStateOf("") }
    var merk by remember { mutableStateOf("") }
    var selectedKategori by remember { mutableStateOf<KategoriProduk?>(null) }
    var selectedSatuan by remember { mutableStateOf<Satuan?>(null) }
    var deskripsi by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var kategoriList by remember { mutableStateOf<List<KategoriProduk>>(emptyList()) }
    var satuanList by remember { mutableStateOf<List<Satuan>>(emptyList()) }

    var expandedKategori by remember { mutableStateOf(false) }
    var expandedSatuan by remember { mutableStateOf(false) }

    // ✅ SOLUSI FINAL: Simpan ID satuan target untuk di-set setelah list loaded
    var targetSatuanId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = uri
        }
    }

    // Load kategori list
    LaunchedEffect(Unit) {
        produkViewModel.getKategoriProduk()

        // If edit mode, load existing produk data
        produkId?.let { id ->
            produkViewModel.getMyProduk()
        }
    }

    // Load kategori list data
    LaunchedEffect(kategoriListState) {
        when (kategoriListState) {
            is Resource.Success -> {
                kategoriList = (kategoriListState as Resource.Success).data ?: emptyList()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (kategoriListState as Resource.Error).message ?: "Gagal load kategori"
                )
            }
            else -> {}
        }
    }

    // Load satuan when kategori selected
    LaunchedEffect(selectedKategori) {
        selectedKategori?.let { kategori ->
            produkViewModel.getSatuanByKategori(kategori.id)

            // ✅ Reset satuan hanya jika bukan dari initial load (targetSatuanId == null)
            if (targetSatuanId == null) {
                selectedSatuan = null
            }
        }
    }

    // ✅ Load satuan list data DAN set selectedSatuan jika ada target
    LaunchedEffect(satuanListState, targetSatuanId) {
        when (satuanListState) {
            is Resource.Success -> {
                satuanList = (satuanListState as Resource.Success).data ?: emptyList()

                // ✅ Jika ada target satuan ID (dari mode edit), set sekarang
                targetSatuanId?.let { id ->
                    selectedSatuan = satuanList.find { it.id == id }
                    targetSatuanId = null // Clear target setelah di-set
                }
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (satuanListState as Resource.Error).message ?: "Gagal load satuan"
                )
            }
            else -> {}
        }
    }

    // Fill form if edit mode
    LaunchedEffect(produkList) {
        produkId?.let { id ->
            val produk = produkList.find { it.id == id }
            produk?.let {
                namaProduk = it.namaProduk ?: ""
                merk = it.merk ?: ""
                deskripsi = it.deskripsi ?: ""
                berat = it.berat?.toString() ?: ""
                harga = it.harga.toInt().toString()
                stok = it.stok.toString()

                // Set selected kategori
                selectedKategori = it.kategoriProduk

                // ✅ Simpan satuan ID untuk di-set nanti setelah list loaded
                targetSatuanId = it.satuan?.id
            }
        }
    }

    // Handle create state
    LaunchedEffect(createProdukState) {
        when (createProdukState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Produk berhasil ditambahkan")
                produkViewModel.resetCreateProdukState()
                onSuccess()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    (createProdukState as Resource.Error).message ?: "Gagal menambahkan produk"
                )
            }
            null -> isLoading = false
        }
    }

    // Handle update state
    LaunchedEffect(updateProdukState) {
        when (updateProdukState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Produk berhasil diupdate")
                produkViewModel.resetUpdateProdukState()
                onSuccess()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    (updateProdukState as Resource.Error).message ?: "Gagal update produk"
                )
            }
            null -> isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (produkId == null) "Tambah Produk" else "Edit Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Informasi Produk",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Kategori Dropdown - FIXED dengan nama kategori
            ExposedDropdownMenuBox(
                expanded = expandedKategori,
                onExpandedChange = { expandedKategori = !expandedKategori && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedKategori?.let { kategori ->
                        // Tampilkan icon + nama kategori
                        buildString {
                            if (!kategori.icon.isNullOrEmpty()) {
                                append(kategori.icon)
                                append(" ")
                            }
                            append(kategori.namaKategori ?: "")
                        }
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori Produk") },
                    placeholder = { Text("Pilih kategori") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isLoading
                )

                ExposedDropdownMenu(
                    expanded = expandedKategori,
                    onDismissRequest = { expandedKategori = false }
                ) {
                    kategoriList.forEach { kategori ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    if (!kategori.icon.isNullOrEmpty()) {
                                        Text(kategori.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(kategori.namaKategori ?: "")
                                }
                            },
                            onClick = {
                                selectedKategori = kategori
                                expandedKategori = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Satuan Dropdown - FIXED agar bisa diklik
            ExposedDropdownMenuBox(
                expanded = expandedSatuan,
                onExpandedChange = {
                    if (selectedKategori != null && !isLoading) {
                        expandedSatuan = !expandedSatuan
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedSatuan?.let { satuan ->
                        "${satuan.namaSatuan ?: ""}${satuan.singkatan?.let { " ($it)" } ?: ""}"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Satuan") },
                    placeholder = { Text("Pilih satuan") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSatuan)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isLoading && selectedKategori != null,
                    supportingText = {
                        if (selectedKategori == null) {
                            Text("Pilih kategori terlebih dahulu", color = MaterialTheme.colorScheme.error)
                        } else if (satuanList.isEmpty()) {
                            Text("Loading satuan...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = expandedSatuan,
                    onDismissRequest = { expandedSatuan = false }
                ) {
                    if (satuanList.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Tidak ada satuan tersedia") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        satuanList.forEach { satuan ->
                            DropdownMenuItem(
                                text = {
                                    Text("${satuan.namaSatuan ?: ""}${satuan.singkatan?.let { " ($it)" } ?: ""}")
                                },
                                onClick = {
                                    selectedSatuan = satuan
                                    expandedSatuan = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = namaProduk,
                onValueChange = { namaProduk = it },
                label = { Text("Nama Produk") },
                placeholder = { Text("Contoh: Urea Petrokimia") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = merk,
                onValueChange = { merk = it },
                label = { Text("Merk/Brand") },
                placeholder = { Text("Contoh: Petrokimia Gresik") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = berat,
                onValueChange = { berat = it },
                label = { Text("Berat/Ukuran (Opsional)") },
                placeholder = { Text("Contoh: 50 (untuk 50kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                supportingText = {
                    Text("Kosongkan jika tidak relevan (misal: alat pertanian)")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi (Opsional)") },
                placeholder = { Text("Jelaskan detail produk...") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Field Harga dengan format ribuan otomatis
            OutlinedTextField(
                value = harga,
                onValueChange = { newValue ->
                    // Hanya izinkan angka
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        harga = newValue
                    }
                },
                label = { Text("Harga per ${selectedSatuan?.namaSatuan ?: "Unit"}") },
                placeholder = { Text("150000") },
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Medium) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandsSeparatorTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                supportingText = {
                    if (harga.isNotEmpty()) {
                        val formatted = try {
                            val number = harga.toLongOrNull() ?: 0
                            val symbols = DecimalFormatSymbols(Locale("id", "ID"))
                            symbols.groupingSeparator = '.'
                            val formatter = DecimalFormat("#,###", symbols)
                            "Rp ${formatter.format(number)}"
                        } catch (e: Exception) {
                            ""
                        }
                        Text(formatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = stok,
                onValueChange = { stok = it },
                label = { Text("Stok (${selectedSatuan?.namaSatuan ?: "unit"})") },
                placeholder = { Text("100") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Upload
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (fotoUri != null) "✓ Foto dipilih" else "Pilih Foto Produk (Opsional)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val beratValue = berat.toDoubleOrNull()
                    val hargaValue = harga.toLongOrNull()?.toDouble() ?: 0.0
                    val stokValue = stok.toIntOrNull() ?: 0

                    if (produkId == null) {
                        // Create new
                        produkViewModel.createProduk(
                            kategoriProdukId = selectedKategori!!.id,
                            satuanId = selectedSatuan!!.id,
                            namaProduk = namaProduk,
                            merk = merk,
                            deskripsi = deskripsi.ifBlank { null },
                            berat = beratValue,
                            harga = hargaValue,
                            stok = stokValue,
                            fotoUri = fotoUri
                        )
                    } else {
                        // Update existing
                        produkViewModel.updateProduk(
                            id = produkId,
                            kategoriProdukId = selectedKategori!!.id,
                            satuanId = selectedSatuan!!.id,
                            namaProduk = namaProduk,
                            merk = merk,
                            deskripsi = deskripsi.ifBlank { null },
                            berat = beratValue,
                            harga = hargaValue,
                            stok = stokValue,
                            status = null,
                            fotoUri = fotoUri
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading &&
                        selectedKategori != null &&
                        selectedSatuan != null &&
                        namaProduk.isNotBlank() &&
                        merk.isNotBlank() &&
                        harga.isNotBlank() &&
                        stok.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (produkId == null) "Tambah Produk" else "Update Produk")
                }
            }
        }
    }
}