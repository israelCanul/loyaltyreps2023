package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable

data class XQuestionChoice(
    var id: Int,
    var option: String?,
    var is_correct: Boolean?,
    var question: Int
) : Parcelable {
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(option)
        parcel.writeBoolean(is_correct)
        parcel.writeInt(question)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<XQuestionChoice> {
        override fun createFromParcel(parcel: Parcel): XQuestionChoice {
            return XQuestionChoice(parcel)
        }

        override fun newArray(size: Int): Array<XQuestionChoice?> {
            return arrayOfNulls(size)
        }
    }
}