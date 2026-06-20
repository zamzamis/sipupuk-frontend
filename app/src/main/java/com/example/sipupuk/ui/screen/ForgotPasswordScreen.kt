package com.example.sipupuk.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sipupuk.ui.viewmodel.AuthViewModel
import com.example.sipupuk.ui.viewmodel.AuthViewModelFactory
import com.example.sipupuk.ui.viewmodel.ValidationHelper
import com.example.sipupuk.util.Resource

private enum class ForgotPasswordStep {
    PHONE_INPUT,
    NEW_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )

    val checkPhoneState by viewModel.checkPhoneState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val focusManager = LocalFocusManager.current

    var step by remember { mutableStateOf(ForgotPasswordStep.PHONE_INPUT) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle Check Phone State
    LaunchedEffect(checkPhoneState) {
        when (checkPhoneState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                step = ForgotPasswordStep.NEW_PASSWORD
                viewModel.resetCheckPhoneState()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    message = (checkPhoneState as Resource.Error).message ?: "Nomor HP tidak terdaftar",
                    duration = SnackbarDuration.Short
                )
            }
            null -> isLoading = false
        }
    }

    // Handle Reset Password State
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                viewModel.resetResetPasswordState()
                onResetSuccess()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    message = (resetPasswordState as Resource.Error).message ?: "Gagal mengubah password",
                    duration = SnackbarDuration.Short
                )
            }
            null -> isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (step == ForgotPasswordStep.PHONE_INPUT) "Lupa Password" else "Buat Password Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                ForgotPasswordStep.PHONE_INPUT -> {
                    Text(
                        text = "Masukkan Nomor HP",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kami akan periksa apakah nomor HP ini terdaftar",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                        },
                        label = { Text("No HP") },
                        placeholder = { Text("08xxxxxxxxxx") },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = "Phone Icon")
                        },
                        isError = phoneError != null,
                        supportingText = {
                            if (phoneError != null) {
                                Text(text = phoneError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            phoneError = ValidationHelper.validatePhone(phone)
                            if (phoneError == null) {
                                focusManager.clearFocus()
                                viewModel.checkPhone(phone)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Lanjutkan", fontSize = 16.sp)
                        }
                    }
                }

                ForgotPasswordStep.NEW_PASSWORD -> {
                    Text(
                        text = "Buat Password Baru",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nomor HP terverifikasi: $phone",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password Baru") },
                        placeholder = { Text("Masukkan password baru") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(text = passwordError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = passwordConfirmation,
                        onValueChange = {
                            passwordConfirmation = it
                            passwordConfirmError = null
                        },
                        label = { Text("Konfirmasi Password") },
                        placeholder = { Text("Ulangi password baru") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                                Icon(
                                    imageVector = if (passwordConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordConfirmVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordConfirmError != null,
                        supportingText = {
                            if (passwordConfirmError != null) {
                                Text(text = passwordConfirmError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            passwordError = ValidationHelper.validatePassword(password)
                            passwordConfirmError = ValidationHelper.validatePasswordConfirmation(password, passwordConfirmation)

                            if (passwordError == null && passwordConfirmError == null) {
                                focusManager.clearFocus()
                                viewModel.resetPassword(phone, password, passwordConfirmation)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Simpan Password Baru", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}