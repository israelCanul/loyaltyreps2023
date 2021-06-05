package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

data class XCanjeResponse(var error: Int, var detalle: String?, var lote: Int, var idSorteo: Int,
                          var idoper: Int, var stock: Int, var dsDescripcionArticulo: String?,
                          var puntosArticulo: Int) : Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(error)
        parcel.writeString(detalle)
        parcel.writeInt(lote)
        parcel.writeInt(idSorteo)
        parcel.writeInt(idoper)
        parcel.writeInt(stock)
        parcel.writeString(dsDescripcionArticulo)
        parcel.writeInt(puntosArticulo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XCanjeResponse> {
        override fun createFromParcel(parcel: Parcel): XCanjeResponse {
            return XCanjeResponse(parcel)
        }

        override fun newArray(size: Int): Array<XCanjeResponse?> {
            return arrayOfNulls(size)
        }
    }
}