package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

data class XUserQuiz (
    var idQuiz: Int,
    var fechaAlta: String?
) : Parcelable {
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idQuiz)
        parcel.writeString(fechaAlta)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XUserQuiz> {
        override fun createFromParcel(parcel: Parcel): XUserQuiz {
            return XUserQuiz(parcel)
        }

        override fun newArray(size: Int): Array<XUserQuiz?> {
            return arrayOfNulls(size)
        }
    }
}