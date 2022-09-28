package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

data class Complimentary (
    var noTarjeta: String?,
    var nombreAfiliado: String?,
    var tarjetaEspecial: String?,
    var parque: String?,
    var idServicio: String?,
    var servicio: String?,
    var noPaxBeneficio: Int,
    var noPaxUtilizado: Int,
    var noPaxPorUtilizar: Int,
    var image: String?,
    var name: String?,
    var phone: String?,
    var action: String?,
    var infants: Boolean,
    var note: String?,
    var order: Int
): Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(noTarjeta)
        parcel.writeString(nombreAfiliado)
        parcel.writeString(tarjetaEspecial)
        parcel.writeString(parque)
        parcel.writeString(idServicio)
        parcel.writeString(servicio)
        parcel.writeInt(noPaxBeneficio)
        parcel.writeInt(noPaxUtilizado)
        parcel.writeInt(noPaxPorUtilizar)
        parcel.writeString(image)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(action)
        parcel.writeBoolean(infants)
        parcel.writeString(note)
        parcel.writeInt(order)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XComplimentary> {
        override fun createFromParcel(parcel: Parcel): XComplimentary {
            return XComplimentary(parcel)
        }

        override fun newArray(size: Int): Array<XComplimentary?> {
            return arrayOfNulls(size)
        }
    }
}