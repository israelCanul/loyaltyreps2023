package com.xcaret.loyaltyreps.model

data class XUserPuntoPos(
    var idAsignacionPuntos: Int,
    var idRep: Int,
    var idUsuario: Int,
    var fecha: String,
    var puntos: Int,
    var comentario: String
)