package com.xcaret.loyaltyreps.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class XComplimentary (
    var servicio: String = "",
    var note: String = "",
    var noPaxPorUtilizar: Int = 0,
    var name: String = "",
    var parque: String?,
    var idServicio: String = "",
    var tarjetaEspecial: String = "",
    var noTarjeta: String = "",
    var noPaxBeneficio: Int = 0,
    var phone: String?,
    var infants: Boolean?,
    var action: String?,
    var nombreAfiliado: String?,
    var noPaxUtilizado: Int?,
    var order: Int?,
    var image: String?
) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this (
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
        //parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(servicio)
        parcel.writeString(note)
        parcel.writeInt(noPaxPorUtilizar)
        parcel.writeString(name)
        parcel.writeString(parque)
        parcel.writeString(idServicio)
        parcel.writeString(tarjetaEspecial)
        parcel.writeString(noTarjeta)
        parcel.writeInt(noPaxBeneficio)
        parcel.writeString(phone)
        parcel.writeBoolean(infants)
        parcel.writeString(action)
        parcel.writeString(nombreAfiliado)
        parcel.writeInt(noPaxUtilizado!!)
        parcel.writeInt(order!!)
        parcel.writeString(image)
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