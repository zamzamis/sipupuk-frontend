package com.example.sipupuk.util

import com.example.sipupuk.data.model.CartItem
import com.example.sipupuk.data.model.Produk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartManager {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    // Add to cart
    fun addToCart(produk: Produk, jumlah: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.produk.id == produk.id }

        if (existingItem != null) {
            // Update jumlah jika produk sudah ada
            existingItem.jumlah += jumlah
        } else {
            // Tambah item baru
            currentItems.add(CartItem(produk, jumlah))
        }

        _cartItems.value = currentItems
        updateCartMetrics()
    }

    // Remove from cart
    fun removeFromCart(produkId: Int) {
        _cartItems.value = _cartItems.value.filter { it.produk.id != produkId }
        updateCartMetrics()
    }

    // Update quantity
    fun updateQuantity(produkId: Int, newJumlah: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val item = currentItems.find { it.produk.id == produkId }

        if (item != null) {
            if (newJumlah <= 0) {
                removeFromCart(produkId)
            } else {
                item.jumlah = newJumlah
                _cartItems.value = currentItems
                updateCartMetrics()
            }
        }
    }

    // Clear cart
    fun clearCart() {
        _cartItems.value = emptyList()
        updateCartMetrics()
    }

    // Get cart item by produk id
    fun getCartItem(produkId: Int): CartItem? {
        return _cartItems.value.find { it.produk.id == produkId }
    }

    // Check if product is in cart
    fun isInCart(produkId: Int): Boolean {
        return _cartItems.value.any { it.produk.id == produkId }
    }

    // Get toko id (all items must be from same toko)
    fun getTokoId(): Int? {
        return _cartItems.value.firstOrNull()?.produk?.tokoId
    }

    // Check if can add product (must be from same toko)
    fun canAddProduct(produk: Produk): Boolean {
        val tokoId = getTokoId()
        return tokoId == null || tokoId == produk.tokoId
    }

    // Private function to update metrics
    private fun updateCartMetrics() {
        val items = _cartItems.value
        _cartCount.value = items.sumOf { it.jumlah }
        _cartTotal.value = items.sumOf { it.produk.harga * it.jumlah }
    }
}