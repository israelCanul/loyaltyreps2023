package com.xcaret.loyaltyreps.model

data class Municipio(
    var idMunicipio: Int,
    var dsMunicipio: String,
    var dsClave: String,
    //var idEstado: Int? = null,
    var cnActivo: Boolean
) {
    override fun toString(): String {
        return this.dsMunicipio
    }
}