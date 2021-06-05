package com.xcaret.loyaltyreps.model

data class XEstadoCivil(
    var idEdoCivil: Int,
    var nombre: String
) {
    override fun toString(): String {
        return this.nombre
    }
}