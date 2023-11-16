package me.timeto.shared.db

import dbsq.EventTemplateSQ

data class EventTemplateDB(
    val id: Int,
    val sort: Int,
    val daytime: Int,
    val text: String,
) {

    companion object {
    }
}

private fun EventTemplateSQ.db() = EventTemplateDB(
    id = id, sort = sort, daytime = daytime, text = text,
)
