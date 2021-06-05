package com.xcaret.loyaltyreps.model

data class XStoreCategory(
    var id: Int,
    var categoryPosition: Int,
    var categoryName: String,
    var categoryProducts: ArrayList<XProduct>
)