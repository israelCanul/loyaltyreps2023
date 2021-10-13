package com.xcaret.loyaltyreps.model

class XUserPuntosNeg(
    var idOperacion: Int,
    var idEdoOperacion: Int,
    var fecha: String,
    var mip: String,
    var puntos: Int,
    var articulo: String,
    var idEstatus: Int = 0,
    var estatus: String = "",
    var observaciones: String = ""
) {
}