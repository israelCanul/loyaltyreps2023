package com.xcaret.loyaltyreps.model

class Hijo(
    var id: Int,
    var desc: String
) {
    override fun toString(): String {
        return this.desc
    }
}