package com.xcaret.loyaltyreps.model

import android.annotation.TargetApi
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class XProduct(
    var id_art: Int,
    var nombre: String?,
    var idCategoriaArticulo: Int,
    var clave: String?,
    var puntos: Int,
    var descripcion: String?,
    var fechalta: String?,
    var foto: String?,
    var thumb: String?,
    var llave: String?,
    var stock: Int,
    var prodmes: Boolean,
    var canjeoModo: Int,
    var esRifa: Boolean,
    var activo: Boolean,
    var feVigencia: String?,
    var cnCurso: Boolean,
    var cnHotSale: Boolean,
    var isVisible: Boolean) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readBoolean(),
        parcel.readInt(),
        parcel.readBoolean(),
        parcel.readBoolean(),
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readBoolean(),
        parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id_art)
        parcel.writeString(nombre)
        parcel.writeInt(idCategoriaArticulo)
        parcel.writeString(clave)
        parcel.writeInt(puntos)
        parcel.writeString(descripcion)
        parcel.writeString(fechalta)
        parcel.writeString(foto)
        parcel.writeString(thumb)
        parcel.writeString(llave)
        parcel.writeInt(stock)
        parcel.writeBoolean(prodmes)
        parcel.writeInt(canjeoModo)
        parcel.writeBoolean(esRifa)
        parcel.writeBoolean(activo)
        parcel.writeString(feVigencia)
        parcel.writeBoolean(cnCurso)
        parcel.writeBoolean(cnHotSale)
        parcel.writeBoolean(isVisible)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XProduct> {
        @TargetApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): XProduct {
            return XProduct(parcel)
        }

        override fun newArray(size: Int): Array<XProduct?> {
            return arrayOfNulls(size)
        }
    }

}