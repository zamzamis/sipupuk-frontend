package com.example.sipupuk.ui.screen.pembeli

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sipupuk.data.model.Produk
import com.example.sipupuk.ui.viewmodel.ProdukViewModel
import com.example.sipupuk.ui.viewmodel.ProdukViewModelFactory
import com.example.sipupuk.util.CartManager
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailProdukScreen(
    produkId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val produkViewModel: ProdukViewModel = viewModel(factory = ProdukViewModelFactory(context))

    val produkList by produkViewModel.produkList.collectAsState()
    val produkListState by produkViewModel.produkListState.collectAsState()

    val produk = produkList.find { it.id == produkId }

    var jumlah by remember { mutableStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    // ✅ Load data produk jika belum ada
    LaunchedEffect(Unit) {
        if (produkList.isEmpty()) {
            produkViewModel.getProduk()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (produk != null && produk.status == "tersedia") {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = { if (jumlah > 1) jumlah-- },
                                modifier = Modifier.size(40.dp),
                                enabled = jumlah > 1,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Kurangi",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "$jumlah ${produk.satuan?.singkatan ?: ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.widthIn(min = 50.dp)
                            )

                            FilledIconButton(
                                onClick = { if (jumlah < produk.stok) jumlah++ },
                                modifier = Modifier.size(40.dp),
                                enabled = jumlah < produk.stok,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Add to Cart Button
                        Button(
                            onClick = {
                                if (CartManager.canAddProduct(produk)) {
                                    CartManager.addToCart(produk, jumlah)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✓ Ditambahkan ke keranjang")
                                    }
                                    jumlah = 1 // Reset quantity
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("⚠️ Hanya bisa pesan dari satu toko")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tambah ke Keranjang")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (produkListState) {
                is Resource.Loading -> {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Memuat detail produk...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    // Error State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (produkListState as Resource.Error).message ?: "Terjadi kesalahan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { produkViewModel.getProduk() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    if (produk == null) {
                        // Product Not Found
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Produk tidak ditemukan",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Produk mungkin sudah dihapus atau tidak tersedia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = onNavigateBack) {
                                    Text("Kembali")
                                }
                            }
                        }
                    } else {
                        // Product Detail Content
                        DetailProdukContent(
                            produk = produk,
                            rupiahFormat = rupiahFormat
                        )
                    }
                }
                null -> {}
            }
        }
    }
}

@Composable
private fun DetailProdukContent(
    produk: Produk,
    rupiahFormat: NumberFormat
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ✅ Product Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!produk.fotoProdukUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = produk.fotoProdukUrl,
                    contentDescription = produk.namaProduk,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Product Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Name
            Text(
                text = produk.namaProduk,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Brand
            Text(
                text = produk.merk,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Chips: Kategori & Berat
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kategori Chip
                produk.kategoriProduk?.let { kategori ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                kategori.icon?.let {
                                    Text(it, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(kategori.namaKategori)
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
                        label = { Text("${berat}kg") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Price with Satuan
            Text(
                text = rupiahFormat.format(produk.harga),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "per ${produk.satuan?.namaSatuan ?: "unit"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Product Details
            Text(
                text = "Detail Produk",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("Merk", produk.merk)

            produk.berat?.let {
                DetailRow("Berat/Ukuran", "${it}kg")
            }

            DetailRow(
                "Stok",
                "${produk.stok} ${produk.satuan?.namaSatuan ?: "unit"}",
                valueColor = if (produk.stok > 10)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            DetailRow(
                "Status",
                produk.status.uppercase(),
                valueColor = when (produk.status) {
                    "tersedia" -> MaterialTheme.colorScheme.primary
                    "habis" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Store Info
            Text(
                text = "Informasi Toko",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = produk.toko?.namaToko ?: "-",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = produk.toko?.desa ?: "-",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ✅ Description (if exists)
            if (!produk.deskripsi.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Deskripsi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = produk.deskripsi,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}