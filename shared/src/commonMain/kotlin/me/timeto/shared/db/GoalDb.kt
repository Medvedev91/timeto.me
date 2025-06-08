package me.timeto.shared.db

import dbsq.GoalSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import me.timeto.shared.vm.goals.form.GoalFormData

data class GoalDb(
    val id: Int,
    val activity_id: Int,
    val seconds: Int,
    val period_json: String,
    val note: String,
    val finish_text: String,
    val home_button_sort: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAll(): List<GoalDb> = dbIo {
            db.goalQueries.selectAll().asList { toDb() }
        }

        fun selectAllFlow(): Flow<List<GoalDb>> =
            db.goalQueries.selectAll().asListFlow { toDb() }

        fun insertManySync(
            activityDb: ActivityDb,
            goalFormsData: List<GoalFormData>,
        ) {
            goalFormsData.forEach { goalFormData ->
                db.goalQueries.insert(
                    activity_id = activityDb.id,
                    seconds = goalFormData.seconds,
                    period_json = goalFormData.period.toJson().toString(),
                    note = goalFormData.note.trim(),
                    finish_text = goalFormData.finishText.trim(),
                    home_button_sort = "",
                )
            }
        }

        fun deleteByActivityDbSync(activityDb: ActivityDb) {
            db.goalQueries.deleteByActivityId(activity_id = activityDb.id)
        }

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
                )
            )
        }
    }

    suspend fun updateHomeButtonSort(
        homeButtonSort: HomeButtonSort,
    ): Unit = dbIo {
        db.goalQueries.updateHomeButtonSortById(
            home_button_sort = homeButtonSort.string,
            id = id,
        )
    }

    fun buildPeriod(): Period =
        Period.fromJson(Json.parseToJsonElement(period_json).jsonObject)

    fun getActivityDbCached(): ActivityDb =
        Cache.getActivityDbByIdOrNull(activity_id)!!

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, activity_id, seconds, period_json,
        note, finish_text, home_button_sort,
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
        )
    }

    override fun backupable__delete() {
        db.goalQueries.deleteById(id)
    }

    ///

    sealed interface Period {

        val type: Type

        fun isToday(): Boolean

        fun note(): String

        fun toJson(): JsonObject

        ///

        enum class Type(val id: Int) {
            daysOfWeek(1), weekly(2),
        }

        companion object {

            fun fromJson(json: JsonObject): Period {
                val typeRaw: Int = json["type"]!!.jsonPrimitive.int
                return when (typeRaw) {
                    Type.daysOfWeek.id -> DaysOfWeek.fromJson(json)
                    Type.weekly.id -> Weekly()
                    else -> throw Exception("GoalDb.Period.fromJson() type: $typeRaw")
                }
            }
        }

        ///

        class DaysOfWeek(
            val days: Set<Int>,
        ) : Period {

            companion object {

                fun fromJson(json: JsonObject) = DaysOfWeek(
                    days = json["days"]!!.jsonArray.map { it.jsonPrimitive.int }.toSet(),
                )

                @Throws(UiException::class)
                fun buildWithValidation(days: Set<Int>): DaysOfWeek {
                    if (days.isEmpty())
                        throw UiException("Days not selected")
                    if (days.any { it !in 0..6 })
                        throw UiException("Invalid days: $days")
                    return DaysOfWeek(days)
                }
            }

            ///

            override val type = Type.daysOfWeek

            override fun isToday(): Boolean =
                UnixTime().dayOfWeek() in days

            override fun note(): String {
                if (days.size == 7)
                    return "Every Day"
                // todo if size is zero?
                return days.sorted().joinToString(", ") { UnixTime.dayOfWeekNames2[it] }
            }

            override fun toJson() = JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type.id),
                    "days" to JsonArray(days.map { JsonPrimitive(it) }),
                )
            )
        }

        class Weekly() : Period {

            override val type = Type.weekly

            override fun isToday(): Boolean = true

            override fun note(): String = "Weekly"

            override fun toJson() = JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type.id),
                )
            )
        }
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
)
