package com.example.sipupuk.ui.screen.pembeli

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
import com.example.sipupuk.data.model.Pesanan
import com.example.sipupuk.ui.viewmodel.PembeliViewModel
import com.example.sipupuk.ui.viewmodel.PembeliViewModelFactory
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananPembeliScreen(
    pesananId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val pembeliViewModel: PembeliViewModel = viewModel(factory = PembeliViewModelFactory(context))

    val myPesananList by pembeliViewModel.myPesananList.collectAsState()
    val myPesananState by pembeliViewModel.myPesananState.collectAsState()
    val cancelPesananState by pembeliViewModel.cancelPesananState.collectAsState()

    val pesanan = myPesananList.find { it.id == pesananId }

    var showCancelDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load data jika belum ada
    LaunchedEffect(Unit) {
        if (myPesananList.isEmpty()) {
            pembeliViewModel.getMyPesanan()
        }
    }

    // Handle cancel result
    LaunchedEffect(cancelPesananState) {
        when (cancelPesananState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Pesanan berhasil dibatalkan")
                pembeliViewModel.resetCancelPesananState()
                pembeliViewModel.getMyPesanan() // Refresh
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (cancelPesananState as Resource.Error).message ?: "Gagal membatalkan pesanan"
                )
                pembeliViewModel.resetCancelPesananState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (myPesananState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Memuat detail pesanan...")
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (myPesananState as Resource.Error).message ?: "Terjadi kesalahan",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { pembeliViewModel.getMyPesanan() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    if (pesanan == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Pesanan tidak ditemukan",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Button(onClick = onNavigateBack) {
                                    Text("Kembali")
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header Info
                            PesananHeaderCard(pesanan)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Store Info
                            TokoInfoCard(pesanan)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Items
                            ItemsCard(pesanan)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Timeline
                            TimelineCard(pesanan)

                            // Cancel Button (only for pending orders)
                            if (pesanan.status == "pending") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showCancelDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Batalkan Pesanan")
                                    }
                                }
                            }
                        }
                    }
                }
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pesanan") },
            text = { Text("Apakah Anda yakin ingin membatalkan pesanan ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pembeliViewModel.cancelPesanan(pesananId)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }
}

@Composable
private fun PesananHeaderCard(pesanan: Pesanan) {
    val rupiahFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val statusColor = when (pesanan.status) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "dikonfirmasi" -> MaterialTheme.colorScheme.primary
        "siap_diambil" -> MaterialTheme.colorScheme.secondary
        "selesai" -> MaterialTheme.colorScheme.primary
        "dibatalkan" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusText = when (pesanan.status) {
        "pending" -> "MENUNGGU KONFIRMASI"
        "dikonfirmasi" -> "SEDANG DIPROSES"
        "siap_diambil" -> "SIAP DIAMBIL"
        "selesai" -> "SELESAI"
        "dibatalkan" -> "DIBATALKAN"
        else -> pesanan.status.uppercase()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pesanan.kodePesanan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Pembayaran",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rupiahFormat.format(pesanan.totalHarga),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Item",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pesanan.totalItem} item",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TokoInfoCard(pesanan: Pesanan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Informasi Toko",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = pesanan.toko?.namaToko ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${pesanan.toko?.desa}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pesanan.catatanToko != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Catatan dari Toko:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pesanan.catatanToko,
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            if (pesanan.catatanPembeli != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Catatan Anda:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pesanan.catatanPembeli,
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ItemsCard(pesanan: Pesanan) {
    val rupiahFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Detail Produk",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            pesanan.detailPesanans?.forEach { detail ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.namaProduk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${detail.merk} ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${rupiahFormat.format(detail.harga)} x ${detail.jumlah}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = rupiahFormat.format(detail.subtotal),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = rupiahFormat.format(pesanan.totalHarga),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TimelineCard(pesanan: Pesanan) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Timeline Pesanan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimelineItem(
                title = "Pesanan Dibuat",
                time = try {
                    dateFormat.format(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(pesanan.createdAt) ?: Date()
                    )
                } catch (e: Exception) {
                    pesanan.createdAt
                },
                icon = Icons.Default.ShoppingCart
            )

            if (pesanan.tanggalKonfirmasi != null) {
                TimelineItem(
                    title = "Dikonfirmasi",
                    time = try {
                        dateFormat.format(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(pesanan.tanggalKonfirmasi) ?: Date()
                        )
                    } catch (e: Exception) {
                        pesanan.tanggalKonfirmasi
                    },
                    note = pesanan.catatanToko,
                    icon = Icons.Default.Check
                )
            }

            if (pesanan.tanggalSiap != null) {
                TimelineItem(
                    title = "Siap Diambil",
                    time = try {
                        dateFormat.format(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(pesanan.tanggalSiap) ?: Date()
                        )
                    } catch (e: Exception) {
                        pesanan.tanggalSiap
                    },
                    icon = Icons.Default.Done
                )
            }

            if (pesanan.tanggalSelesai != null) {
                TimelineItem(
                    title = "Selesai",
                    time = try {
                        dateFormat.format(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(pesanan.tanggalSelesai) ?: Date()
                        )
                    } catch (e: Exception) {
                        pesanan.tanggalSelesai
                    },
                    icon = Icons.Default.DoneAll
                )
            }

            if (pesanan.status == "dibatalkan") {
                TimelineItem(
                    title = "Dibatalkan",
                    time = try {
                        dateFormat.format(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(pesanan.updatedAt) ?: Date()
                        )
                    } catch (e: Exception) {
                        pesanan.updatedAt
                    },
                    icon = Icons.Default.Close
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    title: String,
    time: String,
    note: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.CheckCircle
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (note != null) {
                Text(
                    text = "Catatan: $note",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}