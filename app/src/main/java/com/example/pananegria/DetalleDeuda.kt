package com.example.pananegria

data class DetalleDeuda(

    val id: String = "",

    val deudaId: String = "",

    val clienteId: String = "",

    val productoId: String = "",

    val nombreProducto: String = "",

    val cantidad: Double = 0.0,

    val precioUnitario: Double = 0.0,

    val subtotal: Double = 0.0,

    val unidadMedida: String = "",

    val fecha: Long = 0,

    var pagado: Boolean = false
)