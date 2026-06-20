package com.example.sipupuk.ui.screen.penjual

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokoSetupScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val tokoViewModel: TokoViewModel = viewModel(factory = TokoViewModelFactory(context))

    val tokoState by tokoViewModel.tokoState.collectAsState()
    val createTokoState by tokoViewModel.createTokoState.collectAsState()
    val updateTokoState by tokoViewModel.updateTokoState.collectAsState()
    val currentToko by tokoViewModel.currentToko.collectAsState()
    val hasToko by tokoViewModel.hasToko.collectAsState()

    var namaToko by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var desa by remember { mutableStateOf("") }
    var noTelepon by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamBuka by remember { mutableStateOf("") }
    var jamTutup by remember { mutableStateOf("") }
//    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        uri?.let {
//            // Convert URI to File (simplified version)
//            // In production, use proper URI to File conversion
//            fotoUri = uri
//        }
//    }

    // Load existing toko data
    LaunchedEffect(Unit) {
        tokoViewModel.getMyToko()
    }

    // Fill form with existing data
    LaunchedEffect(currentToko) {
        currentToko?.let { toko ->
            namaToko = toko.namaToko
            alamat = toko.alamat
            desa = toko.desa
            noTelepon = toko.noTelepon
            deskripsi = toko.deskripsi ?: ""
            jamBuka = toko.jamBuka ?.take(5) ?: ""
            jamTutup = toko.jamTutup ?.take(5) ?: ""
        }
    }

    // Handle create state
    LaunchedEffect(createTokoState) {
        when (createTokoState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Toko berhasil dibuat")
                tokoViewModel.resetCreateTokoState()
                onSuccess()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    (createTokoState as Resource.Error).message ?: "Gagal membuat toko"
                )
            }
            null -> isLoading = false
        }
    }

    // Handle update state
    LaunchedEffect(updateTokoState) {
        when (updateTokoState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Toko berhasil diupdate")
                tokoViewModel.resetUpdateTokoState()
                onSuccess()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    (updateTokoState as Resource.Error).message ?: "Gagal update toko"
                )
            }
            null -> isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (hasToko) "Edit Toko" else "Buat Toko") },
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
                text = if (hasToko) "Edit Informasi Toko" else "Lengkapi Informasi Toko",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = namaToko,
                onValueChange = { namaToko = it },
                label = { Text("Nama Toko") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = alamat,
                onValueChange = { alamat = it },
                label = { Text("Alamat Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = desa,
                onValueChange = { desa = it },
                label = { Text("Desa/Kelurahan") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noTelepon,
                onValueChange = { noTelepon = it },
                label = { Text("No. Telepon") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = jamBuka,
                    onValueChange = { jamBuka = it },
                    label = { Text("Jam Buka") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = jamTutup,
                    onValueChange = { jamTutup = it },
                    label = { Text("Jam Tutup") },
                    placeholder = { Text("17:00") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Upload
//            OutlinedButton(
//                onClick = { imagePickerLauncher.launch("image/*") },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isLoading
//            ) {
//                Icon(Icons.Default.Photo, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(if (fotoUri != null) "Foto dipilih" else "Pilih Foto Toko (Opsional)")
//            }

//            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (hasToko) {
                        tokoViewModel.updateToko(
                            namaToko, alamat, desa,
                            noTelepon, deskripsi.ifBlank { null },
                            jamBuka.ifBlank { null }, jamTutup.ifBlank { null },
                            /*fotoUri*/
                        )
                    } else {
                        tokoViewModel.createToko(
                            namaToko, alamat, desa,
                            noTelepon, deskripsi.ifBlank { null },
                            jamBuka.ifBlank { null }, jamTutup.ifBlank { null },
                            /*fotoUri*/
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && namaToko.isNotBlank() && alamat.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (hasToko) "Update Toko" else "Buat Toko")
                }
            }
        }
    }
}