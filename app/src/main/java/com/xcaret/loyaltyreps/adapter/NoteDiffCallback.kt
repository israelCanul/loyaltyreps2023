package com.xcaret.loyaltyreps.adapter

import androidx.recyclerview.widget.DiffUtil
import com.xcaret.loyaltyreps.model.XPark

class NoteDiffCallback(private val oldList: ArrayList<XPark>,
                       private val newList: ArrayList<XPark>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) : Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
