package me.timeto.shared.db

import dbsq.GoalSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.UIException
import me.timeto.shared.UnixTime
import me.timeto.shared.models.GoalFormUi

data class GoalDb(
    val id: Int,
    val activity_id: Int,
    val seconds: Int,
    val period_json: String,
    val note: String,
    val finish_text: String,
) {

    companion object {

        suspend fun selectAll(): List<GoalDb> = dbIo {
            db.goalQueries.selectAll().executeAsList().map { it.toDb() }
        }

        fun selectAllFlow(): Flow<List<GoalDb>> =
            db.goalQueries.selectAll().asListFlow { it.toDb() }

        fun insertManySync(
            activityDb: ActivityDb,
            goalFormsUi: List<GoalFormUi>,
        ) {
            goalFormsUi.forEach { goalFormUi ->
                db.goalQueries.insert(
                    activity_id = activityDb.id,
                    seconds = goalFormUi.seconds,
                    period_json = goalFormUi.period.toJson().toString(),
                    note = goalFormUi.note.trim(),
                    finish_text = goalFormUi.finishText.trim(),
                )
            }
        }

        fun deleteByActivityDbSync(activityDb: ActivityDb) {
            db.goalQueries.deleteByActivityId(activity_id = activityDb.id)
        }
    }

    fun buildPeriod(): Period =
        Period.fromJson(Json.parseToJsonElement(period_json).jsonObject)

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

                fun buildWithValidation(days: Set<Int>): DaysOfWeek {
                    if (days.isEmpty())
                        throw UIException("Days not selected")
                    if (days.any { it !in 0..6 })
                        throw UIException("Invalid days: $days")
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
)
