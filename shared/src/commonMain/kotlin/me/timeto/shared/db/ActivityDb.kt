package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.DaytimeUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.UiException
import me.timeto.shared.UnixTime
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getIntOrNull
import me.timeto.shared.getString
import me.timeto.shared.getStringOrNull
import me.timeto.shared.textFeatures
import me.timeto.shared.toBoolean10
import me.timeto.shared.toInt10
import me.timeto.shared.toJsonArray
import me.timeto.shared.vm.home.buttons.homeButtonsCellsCount
import kotlin.coroutines.cancellation.CancellationException

data class ActivityDb(
    val id: Int,
    val parent_id: Int?,
    val type_id: Int,
    val name: String,
    val goal_json: String?,
    val timer: Int,
    val period_json: String,
    val emoji: String,
    val home_button_sort: String,
    val color_rgba: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
    val checklist_hint: Int,
    val timer_hints: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        fun anyChangeFlow(): Flow<*> =
            db.activityQueries.anyChange().asFlow()

        suspend fun selectAll(): List<ActivityDb> = dbIo {
            db.activityQueries.selectAll().asList { toDb() }
        }

        fun selectAllSync(): List<ActivityDb> =
            db.activityQueries.selectAll().asList { toDb() }

        fun selectAllFlow(): Flow<List<ActivityDb>> =
            db.activityQueries.selectAll().asListFlow { toDb() }

        suspend fun selectByIdOrNull(id: Int): ActivityDb? =
            selectAll().firstOrNull { it.id == id }

        fun selectOtherCached(): ActivityDb =
            Cache.activitiesDb.first { it.type_id == Type.other.id }

        fun selectParentRecursiveMapCached(): Map<Int, List<ActivityDb>> {
            val all = Cache.activitiesDb
            val resMap: Map<Int, MutableList<ActivityDb>> =
                all.associate { it.id to mutableListOf() }
            all.forEach { activityDb ->
                fun addRecursive(parentActivityDb: ActivityDb) {
                    val childrenActivitiesDb =
                        all.filter { it.parent_id == parentActivityDb.id }
                    resMap[activityDb.id]!!.addAll(childrenActivitiesDb)
                    childrenActivitiesDb.forEach { addRecursive(it) }
                }
                addRecursive(activityDb)
            }
            return resMap
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.selectAll().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.activityQueries.insert(
                ActivitySq(
                    id = j.getInt(0),
                    parent_id = j.getIntOrNull(1),
                    type_id = j.getInt(2),
                    name = j.getString(3),
                    goal_json = j.getStringOrNull(4),
                    timer = j.getInt(5),
                    period_json = j.getString(6),
                    emoji = j.getString(7),
                    home_button_sort = j.getString(8),
                    color_rgba = j.getString(9),
                    keep_screen_on = j.getInt(10),
                    pomodoro_timer = j.getInt(11),
                    checklist_hint = j.getInt(12),
                    timer_hints = j.getString(13),
                )
            )
        }
    }

    fun buildGoalOrNull(): Goal? {
        if (goal_json == null)
            return null
        return Goal.fromJson(goal_json)
    }

    suspend fun updateGoal(goal: Goal): Unit = dbIo {
        db.activityQueries.updateGoalById(
            goal_json = goal.toJson(),
            id = id,
        )
    }

    suspend fun updateHomeButtonSort(
        homeButtonSort: HomeButtonSort,
    ): Unit = dbIo {
        db.activityQueries.updateHomeButtonSortById(
            home_button_sort = homeButtonSort.string,
            id = id,
        )
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, parent_id, type_id, name,
        goal_json, timer, period_json, emoji,
        home_button_sort, color_rgba,
        keep_screen_on, pomodoro_timer,
        checklist_hint, timer_hints,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.updateById(
            id = j.getInt(0),
            parent_id = j.getIntOrNull(1),
            type_id = j.getInt(2),
            name = j.getString(3),
            goal_json = j.getStringOrNull(4),
            timer = j.getInt(5),
            period_json = j.getString(6),
            emoji = j.getString(7),
            home_button_sort = j.getString(8),
            color_rgba = j.getString(9),
            keep_screen_on = j.getInt(10),
            pomodoro_timer = j.getInt(11),
            checklist_hint = j.getInt(12),
            timer_hints = j.getString(13),
        )
    }

    override fun backupable__delete() {
        db.activityQueries.deleteById(id)
    }

    ///

    sealed class Goal {

        data class Timer(
            val seconds: Int,
        ) : Goal()

        ///

        companion object {

            fun fromJson(jString: String): Goal {
                val j: JsonObject = Json.parseToJsonElement(jString).jsonObject
                return when (val type = j.getString("type")) {
                    "timer" -> Timer(
                        seconds = j.getInt("seconds"),
                    )
                    else -> throw Exception("Unknown Goal Type: $type")
                }
            }
        }

        fun toJson(): String {
            val jMap: Map<String, JsonElement> = when (val goal = this) {
                is Timer -> mapOf<String, JsonElement>(
                    "type" to JsonPrimitive("timer"),
                    "seconds" to JsonPrimitive(goal.seconds),
                )
            }
            return JsonObject(jMap).toString()
        }
    }
}

private fun ActivitySq.toDb() = ActivityDb(
    id = id, parent_id = parent_id, type_id = type_id, name = name,
    goal_json = goal_json, timer = timer, period_json = period_json,
    emoji = emoji, home_button_sort = home_button_sort,
    color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, checklist_hint = checklist_hint,
    timer_hints = timer_hints,
)
