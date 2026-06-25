package com.example.pananegria

data class Balance(

    var id: String = "",

    var montoManana: Double = 0.0,

    var montoTarde: Double = 0.0,

    var total: Double = 0.0,

    var fecha: Long = 0L
)