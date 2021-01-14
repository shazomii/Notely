package com.davenet.notely.domain

import com.davenet.notely.util.colors

data class NoteEntry(
    var id: Int? = null,
    var title: String = "",
    var text: String = "",
    var date: Long? = null,
    var reminder: Long? = null,
    var started: Boolean = false,
    var color: Int = colors.random()
)