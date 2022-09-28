package com.xcaret.loyaltyreps.model

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class XQuestion(
    var id: Int,
    var choices: ArrayList<XQuestionChoice>,
    var question: String?,
    var quiz: Int
) : Parcelable {
    constructor(parcel: Parcel) : this (
        parcel.readInt(),
        parcel.createTypedArrayList(XQuestionChoice.CREATOR) as ArrayList<XQuestionChoice>,
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeTypedList(choices)
        parcel.writeString(question)
        parcel.writeInt(quiz)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<XQuestion> {
        override fun createFromParcel(parcel: Parcel): XQuestion {
            return XQuestion(parcel)
        }

        override fun newArray(size: Int): Array<XQuestion?> {
            return arrayOfNulls(size)
        }
    }
}