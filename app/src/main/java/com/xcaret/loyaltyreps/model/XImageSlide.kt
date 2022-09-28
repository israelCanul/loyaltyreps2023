package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

data class XImageSlide (
    var name: String?,
    var image: String?
) : Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XImageSlide> {
        override fun createFromParcel(parcel: Parcel): XImageSlide {
            return XImageSlide(parcel)
        }

        override fun newArray(size: Int): Array<XImageSlide?> {
            return arrayOfNulls(size)
        }
    }

}