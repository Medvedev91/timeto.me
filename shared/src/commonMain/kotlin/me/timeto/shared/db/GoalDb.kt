package me.timeto.shared.db

import dbsq.GoalSq
import kotlinx.serialization.json.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.toJsonArray

data class GoalDb(
    val id: Int,
    val activity_id: Int,
    val seconds: Int,
    val period_json: String,
    val note: String,
    val finish_text: String,
    val home_button_sort: String,
    val is_entire_activity: Int,
    val timer: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAll(): List<GoalDb> = dbIo {
            selectAllSync()
        }

        fun selectAllSync(): List<GoalDb> =
            db.goalQueries.selectAll().asList { toDb() }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.goalQueries.selectAll().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.goalQueries.insertSq(
                GoalSq(
                    id = j.getInt(0),
                    activity_id = j.getInt(1),
                    seconds = j.getInt(2),
                    period_json = j.getString(3),
                    note = j.getString(4),
                    finish_text = j.getString(5),
                    home_button_sort = j.getString(6),
                    is_entire_activity = j.getInt(7),
                    timer = j.getInt(8),
                )
            )
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, activity_id, seconds, period_json,
        note, finish_text, home_button_sort,
        is_entire_activity, timer,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.goalQueries.updateById(
            id = j.getInt(0),
            activity_id = j.getInt(1),
            seconds = j.getInt(2),
            period_json = j.getString(3),
            note = j.getString(4),
            finish_text = j.getString(5),
            home_button_sort = j.getString(6),
            is_entire_activity = j.getInt(7),
            timer = j.getInt(8),
        )
    }

    override fun backupable__delete() {
        db.goalQueries.deleteById(id)
    }
}

private fun GoalSq.toDb() = GoalDb(
    id = id,
    activity_id = activity_id,
    seconds = seconds,
    period_json = period_json,
    note = note,
    finish_text = finish_text,
    home_button_sort = home_button_sort,
    is_entire_activity = is_entire_activity,
    timer = timer,
)
