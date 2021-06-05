package com.xcaret.loyaltyreps.model

data class XSupportSubject(
    var id: Int,
    var question: String,
    var faqs: ArrayList<XSQuestion>,
    var expanded: Boolean
)