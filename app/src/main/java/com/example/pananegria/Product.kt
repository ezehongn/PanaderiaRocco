package com.example.pananegria

import java.util.Collections.emptyList

data class Product(

    val id: String = "",

    val nombre: String = "",

    val precio: Double = 0.0,

    val unidadMedida: String = "",

    val categorias: List<String> = emptyList(),

    val imageUrl: String = "",

    val activo: Boolean = true
)
