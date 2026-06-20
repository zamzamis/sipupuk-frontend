package com.example.sipupuk.ui.screen.penjual

import androidx.compose.foundation.background
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
import com.example.sipupuk.data.model.TransaksiLaporan
import com.example.sipupuk.ui.viewmodel.LaporanViewModel
import com.example.sipupuk.ui.viewmodel.LaporanViewModelFactory
import com.example.sipupuk.util.Resource
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanPenjualanScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LaporanViewModel = viewModel(factory = LaporanViewModelFactory(context))
    val laporanState by viewModel.laporanState.collectAsState()

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    // Load data saat pertama kali
    LaunchedEffect(Unit) {
        viewModel.getLaporanBulanan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Penjualan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getLaporanBulanan() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (laporanState) {
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
                val laporan = (laporanState as Resource.Success).data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Periode
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = laporan?.periode?.bulan ?: "Bulan Ini",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Ringkasan Laporan
                    item {
                        Text(
                            text = "Ringkasan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total Pendapatan
                            SummaryCard(
                                icon = Icons.Default.AttachMoney,
                                title = "Total Pendapatan",
                                value = rupiahFormat.format(laporan?.ringkasan?.totalPendapatan ?: 0.0),
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )

                            // Grid untuk 4 metric lainnya
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Total Pesanan
                                SummaryCardSmall(
                                    icon = Icons.Default.ShoppingBag,
                                    title = "Total Pesanan",
                                    value = "${laporan?.ringkasan?.totalPesanan ?: 0}",
                                    modifier = Modifier.weight(1f)
                                )

                                // Pesanan Selesai
                                SummaryCardSmall(
                                    icon = Icons.Default.CheckCircle,
                                    title = "Selesai",
                                    value = "${laporan?.ringkasan?.pesananSelesai ?: 0}",
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Total Item Terjual
                                SummaryCardSmall(
                                    icon = Icons.Default.Inventory,
                                    title = "Item Terjual",
                                    value = "${laporan?.ringkasan?.totalItemTerjual ?: 0}",
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )

                                // Pesanan Dibatalkan
                                SummaryCardSmall(
                                    icon = Icons.Default.Cancel,
                                    title = "Dibatalkan",
                                    value = "${laporan?.ringkasan?.pesananDibatalkan ?: 0}",
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            }
                        }
                    }

                    // Detail Transaksi
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detail Transaksi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${laporan?.transaksi?.size ?: 0} transaksi",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // List Transaksi
                    if (laporan?.transaksi.isNullOrEmpty()) {
                        item {
                            Card {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Belum ada transaksi bulan ini",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(laporan?.transaksi ?: emptyList()) { transaksi ->
                            TransaksiCard(
                                transaksi = transaksi,
                                rupiahFormat = rupiahFormat
                            )
                        }
                    }
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
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (laporanState as Resource.Error).message ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getLaporanBulanan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            null -> {}
        }
    }
}

@Composable
fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SummaryCardSmall(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransaksiCard(
    transaksi: TransaksiLaporan,
    rupiahFormat: NumberFormat
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Kode & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaksi.kodePesanan ?: "-",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Status Badge
                val (statusColor, statusIcon) = when (transaksi.status) {
                    "selesai" -> MaterialTheme.colorScheme.primaryContainer to Icons.Default.CheckCircle
                    "dibatalkan" -> MaterialTheme.colorScheme.errorContainer to Icons.Default.Cancel
                    "pending" -> MaterialTheme.colorScheme.secondaryContainer to Icons.Default.Schedule
                    "dikonfirmasi" -> MaterialTheme.colorScheme.tertiaryContainer to Icons.Default.Check
                    "siap_diambil" -> MaterialTheme.colorScheme.tertiaryContainer to Icons.Default.Inventory
                    else -> MaterialTheme.colorScheme.surfaceVariant to Icons.Default.Info
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = transaksi.status?.uppercase() ?: "UNKNOWN",
                            fontSize = 10.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pembeli & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaksi.namaPembeli ?: "Unknown",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaksi.tanggal ?: "-",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Total & Item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transaksi.totalItem ?: 0} item",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = rupiahFormat.format(transaksi.totalHarga ?: 0.0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}