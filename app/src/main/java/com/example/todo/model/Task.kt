package com.example.todo.model

import android.text.SpannableString

data class Task(
    val content: String,
    val spannableContent: SpannableString,
    val date_and_time:String
)