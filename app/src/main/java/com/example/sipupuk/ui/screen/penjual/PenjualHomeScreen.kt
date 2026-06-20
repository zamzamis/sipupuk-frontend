package com.example.sipupuk.ui.screen.penjual

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.sipupuk.ui.viewmodel.*
import com.example.sipupuk.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenjualHomeScreen(
    onNavigateToTokoSetup: () -> Unit,
    onNavigateToProdukList: () -> Unit,
    onNavigateToPesanan: () -> Unit,
    onNavigatetoLaporan: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val tokoViewModel: TokoViewModel = viewModel(factory = TokoViewModelFactory(context))

    val userData by authViewModel.getUserData().collectAsState(initial = emptyMap())
    val tokoState by tokoViewModel.tokoState.collectAsState()
    val hasToko by tokoViewModel.hasToko.collectAsState()
    val currentToko by tokoViewModel.currentToko.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load toko data
    LaunchedEffect(Unit) {
        tokoViewModel.getMyToko()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Penjual") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Greeting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selamat Datang!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userData["name"] ?: "-",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toko Status
            when (tokoState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is Resource.Success -> {
                    if (currentToko != null) {
                        TokoInfoCard(
                            namaToko = currentToko!!.namaToko,
                            alamat = "${currentToko!!.desa}",
                            status = currentToko!!.status,
                            onEditClick = onNavigateToTokoSetup
                        )
                    }
                }
                is Resource.Error -> {
                    // Toko belum dibuat
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toko Belum Dibuat",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Silakan buat toko Anda terlebih dahulu",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateToTokoSetup) {
                                Text("Buat Toko")
                            }
                        }
                    }
                }
                null -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items
            Text(
                text = "Menu Utama",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toko Menu
            MenuCard(
                icon = Icons.Default.Store,
                title = "Kelola Toko",
                description = "Atur informasi toko Anda",
                onClick = onNavigateToTokoSetup,
                enabled = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Produk Menu
            MenuCard(
                icon = Icons.Default.Inventory,
                title = "Kelola Produk",
                description = "Tambah, edit, atau hapus produk",
                onClick = onNavigateToProdukList,
                enabled = hasToko
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pesanan Menu
            MenuCard(
                icon = Icons.Default.ShoppingCart,
                title = "Kelola Pesanan",
                description = "Lihat dan proses pesanan masuk",
                onClick = onNavigateToPesanan,
                enabled = hasToko
            )

            MenuCard(
                icon = Icons.Default.TableChart,
                title = "Laporan Penjualan",
                description = "Summary dan Detail Transaksi",
                onClick = onNavigatetoLaporan,
                enabled = hasToko
            )

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

@Composable
fun TokoInfoCard(
    namaToko: String,
    alamat: String,
    status: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = namaToko,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alamat,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${status.uppercase()}",
                    fontSize = 12.sp,
                    color = if (status == "aktif")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Toko")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!enabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Buat toko terlebih dahulu",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}