package com.example.sipupuk.ui.screen.pembeli

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.sipupuk.data.model.Pesanan
import com.example.sipupuk.ui.viewmodel.PembeliViewModel
import com.example.sipupuk.ui.viewmodel.PembeliViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananPembeliScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val pembeliViewModel: PembeliViewModel = viewModel(factory = PembeliViewModelFactory(context))

    val myPesananState by pembeliViewModel.myPesananState.collectAsState()
    val myPesananList by pembeliViewModel.myPesananList.collectAsState()
    val cancelPesananState by pembeliViewModel.cancelPesananState.collectAsState()

    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var pesananToCancel by remember { mutableStateOf<Pesanan?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load pesanan
    LaunchedEffect(selectedStatus) {
        pembeliViewModel.getMyPesanan(status = selectedStatus)
    }

    // Handle cancel state
    LaunchedEffect(cancelPesananState) {
        when (cancelPesananState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Pesanan berhasil dibatalkan")
                pembeliViewModel.resetCancelPesananState()
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
                title = { Text("Pesanan Saya") },
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
        ) {
            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = when (selectedStatus) {
                    null -> 0
                    "pending" -> 1
                    "dikonfirmasi" -> 2
                    "siap_diambil" -> 3
                    "selesai" -> 4
                    "dibatalkan" -> 5
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    text = { Text("Semua") }
                )
                Tab(
                    selected = selectedStatus == "pending",
                    onClick = { selectedStatus = "pending" },
                    text = { Text("Pending") }
                )
                Tab(
                    selected = selectedStatus == "dikonfirmasi",
                    onClick = { selectedStatus = "dikonfirmasi" },
                    text = { Text("Dikonfirmasi") }
                )
                Tab(
                    selected = selectedStatus == "siap_diambil",
                    onClick = { selectedStatus = "siap_diambil" },
                    text = { Text("Siap Diambil") }
                )
                Tab(
                    selected = selectedStatus == "selesai",
                    onClick = { selectedStatus = "selesai" },
                    text = { Text("Selesai") }
                )
                Tab(
                    selected = selectedStatus == "dibatalkan",
                    onClick = { selectedStatus = "dibatalkan" },
                    text = { Text("Dibatalkan") }
                )
            }

            Divider()

            // Pesanan List
            when (myPesananState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    if (myPesananList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Belum ada pesanan",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myPesananList) { pesanan ->
                                PesananPembeliCard(
                                    pesanan = pesanan,
                                    onClick = { onNavigateToDetail(pesanan.id) },
                                    onCancel = {
                                        pesananToCancel = pesanan
                                        showCancelDialog = true
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
                                text = (myPesananState as Resource.Error).message ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { pembeliViewModel.getMyPesanan() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelDialog && pesananToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pesanan") },
            text = { Text("Apakah Anda yakin ingin membatalkan pesanan ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pembeliViewModel.cancelPesanan(pesananToCancel!!.id)
                        showCancelDialog = false
                        pesananToCancel = null
                    }
                ) {
                    Text("Ya, Batalkan", color = MaterialTheme.colorScheme.error)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananPembeliCard(
    pesanan: Pesanan,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    }

    val statusColor = when (pesanan.status) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "dikonfirmasi" -> MaterialTheme.colorScheme.primary
        "siap_diambil" -> MaterialTheme.colorScheme.secondary
        "selesai" -> MaterialTheme.colorScheme.primary
        "dibatalkan" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pesanan.toko?.namaToko ?: "-",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pesanan.kodePesanan,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = pesanan.status.uppercase().replace("_", " "),
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.2f),
                        labelColor = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rupiahFormat.format(pesanan.totalHarga),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${pesanan.totalItem} item",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dateFormat.format(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(pesanan.createdAt) ?: Date()
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cancel Button (only for pending)
            if (pesanan.status == "pending") {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Batalkan Pesanan")
                }
            }

            // Status Message
            when (pesanan.status) {
                "siap_diambil" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pesanan siap diambil di toko",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}