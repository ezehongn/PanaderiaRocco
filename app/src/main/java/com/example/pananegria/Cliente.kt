package com.example.pananegria

import java.util.Collections.emptyList

data class Cliente(

    val id: String = "",

    val nombre: String = "",

    val fechaRegistro: Long = 0,

    var deudaTotal: Double = 0.0,

    val apodos: List<String> = emptyList()
)