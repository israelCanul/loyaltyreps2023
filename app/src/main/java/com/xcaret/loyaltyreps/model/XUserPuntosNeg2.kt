package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

class XUserPuntosNeg2(
    var idOperacion: Int,
    var idEdoOperacion: Int,
    var fecha: String?,
    var mip: String?,
    var puntos: Int,
    var articulo: String?,
    var idEstatus: Int,
    var estatus: String?,
    var observaciones: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idOperacion)
        parcel.writeInt(idEdoOperacion)
        parcel.writeString(fecha)
        parcel.writeString(mip)
        parcel.writeInt(puntos)
        parcel.writeString(articulo)
        parcel.writeInt(idEstatus)
        parcel.writeString(estatus)
        parcel.writeString(observaciones)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XUserPuntosNeg2> {
        override fun createFromParcel(parcel: Parcel): XUserPuntosNeg2 {
            return XUserPuntosNeg2(parcel)
        }

        override fun newArray(size: Int): Array<XUserPuntosNeg2?> {
            return arrayOfNulls(size)
        }
    }

}