package com.xcaret.loyaltyreps.model

import java.util.ArrayList

data class XQuiz(
    var id: Int,
    var questions: ArrayList<XQuestion>,
    var wallet: Int,
    var name: String?,
    var points: Int,
    var main_quiz: Boolean?,
    var video: Int
)