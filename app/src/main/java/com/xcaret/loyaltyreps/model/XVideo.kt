package com.xcaret.loyaltyreps.model

data class XVideo(
    var id: Int,
    val name: String?,
    var video: String?,
    var cover_img: String?,
    var points: Int,
    var active: Boolean?,
    var wallet: Int,
    var quiz_id: Int,
    var deadline: String,
    var quiz_available: Boolean,
    var visibility: Boolean
)