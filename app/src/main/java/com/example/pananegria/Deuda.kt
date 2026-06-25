package com.example.pananegria

data class Deuda(

    val id: String = "",

    val clienteId: String = "",

    val clienteNombre: String = "",

    val fecha: Long = 0,

    val total: Double = 0.0,

    val estado: String = "pendiente"
)