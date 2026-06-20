package com.example.sipupuk.ui.screen.pembeli

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.sipupuk.data.model.KategoriProduk
import com.example.sipupuk.data.model.Produk
import com.example.sipupuk.ui.viewmodel.*
import com.example.sipupuk.util.CartManager
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembeliHomeScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToPesanan: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val produkViewModel: ProdukViewModel = viewModel(factory = ProdukViewModelFactory(context))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    val produkListState by produkViewModel.produkListState.collectAsState()
    val produkList by produkViewModel.produkList.collectAsState()
    val kategoriListState by produkViewModel.kategoriListState.collectAsState()
    val cartCount by CartManager.cartCount.collectAsState()
    val userData by authViewModel.getUserData().collectAsState(initial = emptyMap())


    val kategoriList = (kategoriListState as? Resource.Success)?.data ?: emptyList()
    var selectedKategori by remember { mutableStateOf<KategoriProduk?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Load kategori dari API
    LaunchedEffect(Unit) {
        produkViewModel.getKategoriProduk()
    }

    // ✅ Handle kategori state changes
    LaunchedEffect(kategoriListState) {
        when (kategoriListState) {
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (kategoriListState as Resource.Error).message ?: "Gagal load kategori"
                )
            }
            else -> {}
        }
    }

    // ✅ Load produk berdasarkan kategori & search
    LaunchedEffect(selectedKategori, searchQuery) {
        produkViewModel.getProduk(
            perPage = 100,
            kategoriId = selectedKategori?.id,
            search = searchQuery.ifBlank { null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SI PUPUK", fontSize = 18.sp)
                        Text(
                            text = userData["name"] ?: "Pembeli",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Cart Icon
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge { Text(cartCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                        }
                    }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
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
        ) {
            // Quick Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    onClick = onNavigateToPesanan,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pesanan Saya",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedCard(
                    onClick = onNavigateToCart,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge { Text(cartCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Keranjang",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Cari produk...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ FILTER KATEGORI - HORIZONTAL SCROLLABLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Kategori Produk",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                when (kategoriListState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tombol "Semua"
                            item {
                                FilterChip(
                                    selected = selectedKategori == null,
                                    onClick = { selectedKategori = null },
                                    label = { Text("Semua") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.GridView,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }

                            // List Kategori dari Database
                            items(kategoriList) { kategori ->
                                FilterChip(
                                    selected = selectedKategori?.id == kategori.id,
                                    onClick = { selectedKategori = kategori },
                                    label = {
                                        Text(text = kategori.namaKategori ?: "Kategori")
                                    },
                                    leadingIcon = {
                                        kategori.icon?.takeIf { it.isNotBlank() }?.let {
                                            Text(text = it, fontSize = 16.sp)
                                        }
                                    }
                                )
                            }

                        }
                    }
                    is Resource.Error -> {
                        Text(
                            text = "Gagal memuat kategori",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    null -> {}
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "Produk tidak ditemukan"
                                    } else {
                                        "Belum ada produk di kategori ini"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(produkList) { produk ->
                                ProdukCard(
                                    produk = produk,
                                    onClick = { onNavigateToDetail(produk.id) }
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (produkListState as Resource.Error).message ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                produkViewModel.getProduk(kategoriId = selectedKategori?.id)
                            }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukCard(
    produk: Produk,
    onClick: () -> Unit
) {
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // FOTO / ICON
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
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama Produk
            Text(
                text = produk.namaProduk ?: "Produk",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Merk
            Text(
                text = produk.merk ?: "Umum",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Harga
            Text(
                text = rupiahFormat.format(produk.harga),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Satuan
            Text(
                text = "per ${produk.satuan?.namaSatuan ?: "unit"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stok & Kategori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stok: ${produk.stok}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Kategori Chip
                produk.kategoriProduk?.let { kategori ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!kategori.icon.isNullOrEmpty()) {
                                    Text(kategori.icon, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = kategori.namaKategori ?: "",
                                    fontSize = 10.sp
                                )
                            }
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Berat (jika ada)
            produk.berat?.let { berat ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${berat}kg",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}