package com.example.sipupuk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sipupuk.data.model.LaporanBulanan
import com.example.sipupuk.ui.screen.LoginScreen
import com.example.sipupuk.ui.screen.RegisterScreen
import com.example.sipupuk.ui.screen.ForgotPasswordScreen
import com.example.sipupuk.ui.screen.pembeli.*
import com.example.sipupuk.ui.screen.penjual.LaporanPenjualanScreen
import com.example.sipupuk.ui.screen.penjual.PenjualHomeScreen
import com.example.sipupuk.ui.screen.penjual.TokoSetupScreen
import com.example.sipupuk.ui.screen.penjual.ProdukListScreen
import com.example.sipupuk.ui.screen.penjual.TambahProdukScreen
import com.example.sipupuk.ui.screen.penjual.PesananListScreen
import com.example.sipupuk.ui.screen.penjual.PesananDetailScreen
import com.example.sipupuk.ui.viewmodel.AuthViewModel
import com.example.sipupuk.ui.viewmodel.AuthViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object ForgotPassword : Screen("forgot_password")

    // Pembeli Routes
    object PembeliHome : Screen("pembeli_home")
    object DetailProduk : Screen("detail_produk/{produkId}") {
        fun createRoute(produkId: Int) = "detail_produk/$produkId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object PesananPembeli : Screen("pesanan_pembeli")
    object DetailPesananPembeli : Screen("detail_pesanan_pembeli/{pesananId}") {
        fun createRoute(pesananId: Int) = "detail_pesanan_pembeli/$pesananId"
    }

    // Penjual Routes
    object PenjualHome : Screen("penjual_home")
    object TokoSetup : Screen("toko_setup")
    object ProdukList : Screen("produk_list")
    object Laporan : Screen("Laporan")
    object TambahProduk : Screen("tambah_produk?produkId={produkId}") {
        fun createRoute(produkId: Int?) = "tambah_produk?produkId=$produkId"
    }
    object PesananList : Screen("pesanan_list")

    object PesananDetail : Screen("pesanan_detail/{pesananId}") {
        fun createRoute(pesananId: Int) = "pesanan_detail/$pesananId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userData by viewModel.getUserData().collectAsState(initial = emptyMap())
    val userRole = userData["role"] ?: ""

    // Determine start destination based on login status and role
    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        userRole == "penjual" -> Screen.PenjualHome.route
        userRole == "pembeli" -> Screen.PembeliHome.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Routes
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate based on role
                    when (userRole) {
                        "penjual" -> {
                            navController.navigate(Screen.PenjualHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "pembeli" -> {
                            navController.navigate(Screen.PembeliHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    when (userRole) {
                        "penjual" -> {
                            navController.navigate(Screen.PenjualHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "pembeli" -> {
                            navController.navigate(Screen.PembeliHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onResetSuccess = {
                    // Kembali ke halaman login setelah password berhasil diubah
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pembeli Home (existing)
        composable(Screen.PembeliHome.route) {
            PembeliHomeScreen(
                onNavigateToDetail = { produkId ->
                    navController.navigate(Screen.DetailProduk.createRoute(produkId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToPesanan = {
                    navController.navigate(Screen.PesananPembeli.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DetailProduk.route,
            arguments = listOf(
                navArgument("produkId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val produkId = backStackEntry.arguments?.getInt("produkId") ?: 0

            DetailProdukScreen(
                produkId = produkId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }


        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.navigate(Screen.PesananPembeli.route) {
                        popUpTo(Screen.PembeliHome.route)
                    }
                }
            )
        }

        composable(Screen.PesananPembeli.route) {
            PesananPembeliScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { pesananId ->
                    navController.navigate(Screen.DetailPesananPembeli.createRoute(pesananId))
                }
            )
        }

        composable(
            route = Screen.DetailPesananPembeli.route,
            arguments = listOf(
                navArgument("pesananId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val pesananId = backStackEntry.arguments?.getInt("pesananId") ?: 0

            // Reuse PesananDetailScreen from penjual (same UI)
            DetailPesananPembeliScreen(
                pesananId = pesananId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Penjual Routes
        composable(Screen.PenjualHome.route) {
            PenjualHomeScreen(
                onNavigateToTokoSetup = {
                    navController.navigate(Screen.TokoSetup.route)
                },
                onNavigateToProdukList = {
                    navController.navigate(Screen.ProdukList.route)
                },
                onNavigateToPesanan = {
                    navController.navigate(Screen.PesananList.route)
                },
                onNavigatetoLaporan = {
                    navController.navigate(Screen.Laporan.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TokoSetup.route) {
            TokoSetupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProdukList.route) {
            ProdukListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTambahProduk = { produkId ->
                    navController.navigate(Screen.TambahProduk.createRoute(produkId))
                }
            )
        }

        composable(
            route = Screen.TambahProduk.route,
            arguments = listOf(
                navArgument("produkId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val produkIdString = backStackEntry.arguments?.getString("produkId")
            val produkId = produkIdString?.toIntOrNull()

            TambahProdukScreen(
                produkId = produkId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PesananList.route) {
            PesananListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPesananDetail = { pesananId ->
                    navController.navigate(Screen.PesananDetail.createRoute(pesananId))
                }
            )
        }

        composable(
            route = Screen.PesananDetail.route,
            arguments = listOf(
                navArgument("pesananId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val pesananId = backStackEntry.arguments?.getInt("pesananId") ?: 0

            PesananDetailScreen(
                pesananId = pesananId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Laporan.route){
            LaporanPenjualanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}