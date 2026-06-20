package com.example.sipupuk.ui.screen.penjual

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
import com.example.sipupuk.ui.viewmodel.PesananViewModel
import com.example.sipupuk.ui.viewmodel.PesananViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPesananDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val pesananViewModel: PesananViewModel = viewModel(factory = PesananViewModelFactory(context))

    val pesananListState by pesananViewModel.pesananListState.collectAsState()
    val pesananList by pesananViewModel.pesananList.collectAsState()
    val statisticsState by pesananViewModel.statisticsState.collectAsState()
    val statistics by pesananViewModel.statistics.collectAsState()

    var selectedStatus by remember { mutableStateOf<String?>(null) }

    // Load pesanan
    LaunchedEffect(selectedStatus) {
        pesananViewModel.getTokoPesanan(status = selectedStatus)
        pesananViewModel.getStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Statistics Cards
            if (statistics != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Pending",
                                count = statistics!!.pending,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Proses",
                                count = statistics!!.dikonfirmasi,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Siap",
                                count = statistics!!.siapDiambil,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Selesai",
                                count = statistics!!.selesai,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

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
            when (pesananListState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    if (pesananList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
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
                            items(pesananList) { pesanan ->
                                PesananItem(
                                    pesanan = pesanan,
                                    onClick = { onNavigateToPesananDetail(pesanan.id) }
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
                                text = (pesananListState as Resource.Error).message ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { pesananViewModel.getTokoPesanan() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananItem(
    pesanan: Pesanan,
    onClick: () -> Unit
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
                    Text(
                        text = pesanan.kodePesanan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pesanan.user?.name ?: "-",
                        fontSize = 14.sp,
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

            if (pesanan.catatanPembeli != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${pesanan.catatanPembeli}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}