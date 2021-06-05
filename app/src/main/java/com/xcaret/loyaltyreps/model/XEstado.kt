package com.xcaret.loyaltyreps.model

data class XEstado (
    var idEstado: Int,
    var dsEstado: String,
    var idPais: Int,
    var idIdioma: Int
) {
    override fun toString(): String {
        return this.dsEstado
    }
}