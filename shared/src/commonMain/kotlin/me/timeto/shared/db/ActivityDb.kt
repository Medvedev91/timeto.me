package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.toJsonArray

data class ActivityDb(
    val id: Int,
    val name: String,
    val emoji: String,
    val timer: Int,
    val sort: Int,
    val type_id: Int,
    val color_rgba: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
    val timer_hints: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        fun anyChangeFlow(): Flow<*> =
            db.activityQueries.anyChange().asFlow()

        private fun selectSortedSync(): List<ActivityDb> =
            db.activityQueries.selectSorted().asList { toDb() }

        suspend fun selectSorted(): List<ActivityDb> = dbIo {
            selectSortedSync()
        }

        fun selectByIdOrNullSync(id: Int): ActivityDb? =
            selectSortedSync().firstOrNull { it.id == id }

        suspend fun selectByIdOrNull(id: Int): ActivityDb? = dbIo {
            selectByIdOrNullSync(id)
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.selectSorted().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.activityQueries.insert(
                ActivitySQ(
                    id = j.getInt(0),
                    name = j.getString(1),
                    timer = j.getInt(2),
                    sort = j.getInt(3),
                    type_id = j.getInt(4),
                    color_rgba = j.getString(5),
                    emoji = j.getString(6),
                    keep_screen_on = j.getInt(7),
                    pomodoro_timer = j.getInt(8),
                    timer_hints = j.getString(9),
                )
            )
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, timer, sort, type_id, color_rgba,
        emoji, keep_screen_on, pomodoro_timer, timer_hints,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.updateById(
            id = j.getInt(0),
            name = j.getString(1),
            timer = j.getInt(2),
            sort = j.getInt(3),
            type_id = j.getInt(4),
            color_rgba = j.getString(5),
            emoji = j.getString(6),
            keep_screen_on = j.getInt(7),
            pomodoro_timer = j.getInt(8),
            timer_hints = j.getString(9),
        )
    }

    override fun backupable__delete() {
        db.activityQueries.deleteById(id)
    }
}

///

private fun ActivitySQ.toDb() = ActivityDb(
    id = id, name = name, emoji = emoji, timer = timer, sort = sort,
    type_id = type_id, color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, timer_hints = timer_hints,
)
