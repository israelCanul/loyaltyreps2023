package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

fun Parcel.writeBoolean(flag: Boolean?) {
    when(flag) {
        true -> writeInt(1)
        false -> writeInt(0)
        else -> writeInt(-1)
    }
}

fun Parcel.readBoolean(): Boolean? {
    return when(readInt()) {
        1 -> true
        0 -> false
        else -> null
    }
}

data class XPark(var id: Int, val name: String, var logo: String,
                 var color: String, var infographics: ArrayList<XParkInfographic>)

data class XParkInfographic(var id: Int, var language: String?, var image: String?, var park: Int) : Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(language)
        parcel.writeString(image)
        parcel.writeInt(park)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XParkInfographic> {
        override fun createFromParcel(parcel: Parcel): XParkInfographic {
            return XParkInfographic(parcel)
        }

        override fun newArray(size: Int): Array<XParkInfographic?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return this.language!!
    }
}

data class XTour(var idProducto: Int, var dsDescripcion: String, var dsClave: String,
                 var cnAllotmentHorario: Boolean, var idLocacion: Int, var idUnidadNegocio: Int) {
    override fun toString(): String {
        return this.dsDescripcion
    }
}

data class XTourSchedule(var retValue: Int, var horario: String){
    override fun toString(): String {
        return this.horario
    }
}

data class XZone(var idUbicacionGeografica: Int,
                 var dsClave: String,
                 var dsUbicacionGeografica: String,
                 var cnActivo: Boolean,
                 var feAlta: String,
                 var idClienteUsuarioAlta: Int,
                 var prIVA: Int) {
    override fun toString(): String {
        return this.dsUbicacionGeografica
    }
}

data class PickUpHotel(var idHotel: Int, var dsNombreHotel: String,
                       var idHotelPickupHorarioProducto: Int, var hrPickup: String)

data class XPCategory(var idCategoriaArticulo: Int, var dsDescripcion: String, var feAlta: String){
    override fun toString(): String {
        return this.dsDescripcion
    }
}

data class XTraining(var id: Int, val name: String?, var description: String?,
                     var training_section: Int) : Parcelable {
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(training_section)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XTraining> {
        override fun createFromParcel(parcel: Parcel): XTraining {
            return XTraining(parcel)
        }

        override fun newArray(size: Int): Array<XTraining?> {
            return arrayOfNulls(size)
        }
    }
}

data class XNews(var id: Int, var cover_img: String?, var title: String?, var description: String?,
                 var created_at: String?, var updated_at: String?) : Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(cover_img)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(created_at)
        parcel.writeString(updated_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XNews> {
        override fun createFromParcel(parcel: Parcel): XNews {
            return XNews(parcel)
        }

        override fun newArray(size: Int): Array<XNews?> {
            return arrayOfNulls(size)
        }
    }
}
