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
import com.example.sipupuk.data.model.Pesanan
import com.example.sipupuk.ui.viewmodel.PesananViewModel
import com.example.sipupuk.ui.viewmodel.PesananViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananDetailScreen(
    pesananId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val pesananViewModel: PesananViewModel = viewModel(factory = PesananViewModelFactory(context))

    val pesananDetailState by pesananViewModel.pesananDetailState.collectAsState()
    val actionState by pesananViewModel.actionState.collectAsState()

    var showKonfirmasiDialog by remember { mutableStateOf(false) }
    var showSiapDialog by remember { mutableStateOf(false) }
    var showSelesaiDialog by remember { mutableStateOf(false) }
    var showTolakDialog by remember { mutableStateOf(false) }
    var catatanToko by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load pesanan detail
    LaunchedEffect(pesananId) {
        pesananViewModel.getPesananDetail(pesananId)
    }

    // Handle action state
    LaunchedEffect(actionState) {
        when (actionState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Pesanan berhasil diupdate")
                pesananViewModel.resetActionState()
                pesananViewModel.getPesananDetail(pesananId) // Refresh
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (actionState as Resource.Error).message ?: "Gagal update pesanan"
                )
                pesananViewModel.resetActionState()
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
        when (pesananDetailState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val pesanan = (pesananDetailState as Resource.Success<Pesanan>).data!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Info
                    PesananHeaderCard(pesanan)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Customer Info
                    CustomerInfoCard(pesanan)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Items
                    ItemsCard(pesanan)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Timeline
                    TimelineCard(pesanan)

                    // Actions
                    ActionsSection(
                        pesanan = pesanan,
                        onKonfirmasi = { showKonfirmasiDialog = true },
                        onSiapDiambil = { showSiapDialog = true },
                        onSelesai = { showSelesaiDialog = true },
                        onTolak = { showTolakDialog = true }
                    )
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (pesananDetailState as Resource.Error).message ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { pesananViewModel.getPesananDetail(pesananId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            null -> {}
        }
    }

    // Dialogs
    if (showKonfirmasiDialog) {
        AlertDialog(
            onDismissRequest = { showKonfirmasiDialog = false },
            title = { Text("Konfirmasi Pesanan") },
            text = {
                Column {
                    Text("Konfirmasi pesanan ini untuk diproses?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catatanToko,
                        onValueChange = { catatanToko = it },
                        label = { Text("Catatan (Opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pesananViewModel.konfirmasiPesanan(pesananId, catatanToko.ifBlank { null })
                        showKonfirmasiDialog = false
                        catatanToko = ""
                    }
                ) {
                    Text("Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKonfirmasiDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showSiapDialog) {
        AlertDialog(
            onDismissRequest = { showSiapDialog = false },
            title = { Text("Pesanan Siap Diambil") },
            text = { Text("Tandai pesanan ini siap diambil?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pesananViewModel.siapDiambilPesanan(pesananId)
                        showSiapDialog = false
                    }
                ) {
                    Text("Ya, Siap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSiapDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showSelesaiDialog) {
        AlertDialog(
            onDismissRequest = { showSelesaiDialog = false },
            title = { Text("Selesaikan Pesanan") },
            text = { Text("Pesanan sudah diambil dan dibayar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pesananViewModel.selesaiPesanan(pesananId)
                        showSelesaiDialog = false
                    }
                ) {
                    Text("Ya, Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSelesaiDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showTolakDialog) {
        AlertDialog(
            onDismissRequest = { showTolakDialog = false },
            title = { Text("Tolak Pesanan") },
            text = {
                Column {
                    Text("Berikan alasan penolakan:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catatanToko,
                        onValueChange = { catatanToko = it },
                        label = { Text("Alasan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (catatanToko.isNotBlank()) {
                            pesananViewModel.tolakPesanan(pesananId, catatanToko)
                            showTolakDialog = false
                            catatanToko = ""
                        }
                    },
                    enabled = catatanToko.isNotBlank()
                ) {
                    Text("Tolak", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTolakDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun PesananHeaderCard(pesanan: Pesanan) {
    val rupiahFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val statusColor = when (pesanan.status) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "dikonfirmasi" -> MaterialTheme.colorScheme.primary
        "siap_diambil" -> MaterialTheme.colorScheme.secondary
        "selesai" -> MaterialTheme.colorScheme.primary
        "dibatalkan" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = pesanan.status.uppercase().replace("_", " "),
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
fun CustomerInfoCard(pesanan: Pesanan) {
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
                text = "Informasi Pembeli",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Nama", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = pesanan.user?.name ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Telepon", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = pesanan.user?.phone ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (pesanan.catatanPembeli != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Catatan Pembeli:",
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
fun ItemsCard(pesanan: Pesanan) {
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

            Divider()

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
fun TimelineCard(pesanan: Pesanan) {
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
                text = "Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimelineItem(
                title = "Pesanan Dibuat",
                time = dateFormat.format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .parse(pesanan.createdAt) ?: Date()
                )
            )

            if (pesanan.tanggalKonfirmasi != null) {
                TimelineItem(
                    title = "Dikonfirmasi",
                    time = dateFormat.format(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(pesanan.tanggalKonfirmasi) ?: Date()
                    ),
                    note = pesanan.catatanToko
                )
            }

            if (pesanan.tanggalSiap != null) {
                TimelineItem(
                    title = "Siap Diambil",
                    time = dateFormat.format(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(pesanan.tanggalSiap) ?: Date()
                    )
                )
            }

            if (pesanan.tanggalSelesai != null) {
                TimelineItem(
                    title = "Selesai",
                    time = dateFormat.format(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(pesanan.tanggalSelesai) ?: Date()
                    )
                )
            }
        }
    }
}

@Composable
fun TimelineItem(title: String, time: String, note: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.CheckCircle,
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

@Composable
fun ActionsSection(
    pesanan: Pesanan,
    onKonfirmasi: () -> Unit,
    onSiapDiambil: () -> Unit,
    onSelesai: () -> Unit,
    onTolak: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (pesanan.status) {
            "pending" -> {
                Button(
                    onClick = onKonfirmasi,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi Pesanan")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onTolak,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tolak Pesanan")
                }
            }
            "dikonfirmasi" -> {
                Button(
                    onClick = onSiapDiambil,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Siap Diambil")
                }
            }
            "siap_diambil" -> {
                Button(
                    onClick = onSelesai,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selesaikan Pesanan")
                }
            }
        }
    }
}