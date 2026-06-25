package com.example.pananegria

data class Movimiento(

    var cliente: String = "",

    var descripcion: String = "",

    var monto: Double = 0.0,

    var usuario: String = "",

    var fecha: Long = 0
)