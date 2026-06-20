package com.example.sipupuk.ui.screen.penjual

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sipupuk.data.model.KategoriProduk
import com.example.sipupuk.data.model.Produk
import com.example.sipupuk.ui.viewmodel.ProdukViewModel
import com.example.sipupuk.ui.viewmodel.ProdukViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTambahProduk: (Int?) -> Unit // null = tambah baru, id = edit
) {
    val context = LocalContext.current
    val produkViewModel: ProdukViewModel = viewModel(factory = ProdukViewModelFactory(context))

    val produkListState by produkViewModel.produkListState.collectAsState()
    val produkList by produkViewModel.produkList.collectAsState()
    val deleteProdukState by produkViewModel.deleteProdukState.collectAsState()
    val kategoriListState by produkViewModel.kategoriListState.collectAsState()

    // ✅ PERBAIKAN: Langsung ambil dari state (auto-recompose)
    val kategoriList = (kategoriListState as? Resource.Success)?.data ?: emptyList()
    var selectedKategori by remember { mutableStateOf<KategoriProduk?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var produkToDelete by remember { mutableStateOf<Produk?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load kategori list
    LaunchedEffect(Unit) {
        produkViewModel.getKategoriProduk()
    }

    // Load produk dengan filter
    LaunchedEffect(selectedKategori, selectedStatus, searchQuery) {
        produkViewModel.getMyProduk(
            perPage = 100,
            kategoriId = selectedKategori?.id,
            status = selectedStatus,
            search = searchQuery.ifBlank { null }
        )
    }

    // Handle delete state
    LaunchedEffect(deleteProdukState) {
        when (deleteProdukState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Produk berhasil dihapus")
                produkViewModel.resetDeleteProdukState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (deleteProdukState as Resource.Error).message ?: "Gagal menghapus produk"
                )
                produkViewModel.resetDeleteProdukState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTambahProduk(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search & Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari produk") },
                    placeholder = { Text("Nama produk, merk...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter by Kategori
                Text(
                    text = "Filter Kategori",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ PERBAIKAN: LazyRow untuk scroll horizontal
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    // Chip: Semua
                    item {
                        FilterChip(
                            selected = selectedKategori == null,
                            onClick = { selectedKategori = null },
                            label = { Text("Semua") }
                        )
                    }

                    // Chips: Kategori dari API
                    items(kategoriList) { kategori ->
                        FilterChip(
                            selected = selectedKategori?.id == kategori.id,
                            onClick = {
                                selectedKategori = if (selectedKategori?.id == kategori.id) {
                                    null
                                } else {
                                    kategori
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!kategori.icon.isNullOrEmpty()) {
                                        Text(kategori.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(kategori.namaKategori ?: "")
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter by Status
                Text(
                    text = "Filter Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("Semua") }
                    )

                    FilterChip(
                        selected = selectedStatus == "tersedia",
                        onClick = {
                            selectedStatus = if (selectedStatus == "tersedia") null else "tersedia"
                        },
                        label = { Text("Tersedia") }
                    )

                    FilterChip(
                        selected = selectedStatus == "habis",
                        onClick = {
                            selectedStatus = if (selectedStatus == "habis") null else "habis"
                        },
                        label = { Text("Habis") }
                    )
                }
            }

            HorizontalDivider()

            // Product List
            when (produkListState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    if (produkList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "Produk tidak ditemukan"
                                    } else {
                                        "Belum ada produk"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(produkList) { produk ->
                                ProdukItem(
                                    produk = produk,
                                    onEditClick = { onNavigateToTambahProduk(produk.id) },
                                    onDeleteClick = {
                                        produkToDelete = produk
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = (produkListState as Resource.Error).message ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { produkViewModel.getMyProduk() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && produkToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Produk") },
            text = { Text("Apakah Anda yakin ingin menghapus ${produkToDelete!!.namaProduk ?: "produk ini"}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        produkViewModel.deleteProduk(produkToDelete!!.id)
                        showDeleteDialog = false
                        produkToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukItem(
    produk: Produk,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Foto Produk
            produk.fotoProdukUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = produk.namaProduk,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp)
                )
            }

            // Info Produk
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = produk.namaProduk ?: "Produk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = produk.merk ?: "Umum",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = rupiahFormat.format(produk.harga),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "per ${produk.satuan?.namaSatuan ?: "unit"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Stok: ${produk.stok} ${produk.satuan?.singkatan ?: ""}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = produk.status?.uppercase() ?: "UNKNOWN",
                            fontSize = 12.sp,
                            color = when (produk.status) {
                                "tersedia" -> MaterialTheme.colorScheme.primary
                                "habis" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chips: Kategori & Berat - ✅ FIXED NULL SAFETY
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kategori Chip
                    produk.kategoriProduk?.let { kategori ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // ✅ FIX: Check if icon is not null or empty
                                    if (!kategori.icon.isNullOrEmpty()) {
                                        Text(kategori.icon, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = kategori.namaKategori ?: "",
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }

                    // Berat Chip (jika ada)
                    produk.berat?.let { berat ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "${berat}kg",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}