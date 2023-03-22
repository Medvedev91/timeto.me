package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dbsq.KVSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import timeto.shared.*

data class KVModel(
    val key: String,
    val value: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val DAY_START_OFFSET_SECONDS_DEFAULT = 0

        suspend fun getAll() = dbIO {
            db.kVQueries.getAll().executeAsList().map { it.toModel() }
        }

        fun getAllFlow() = db.kVQueries.getAll().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        fun getByKeyOrNullFlow(key: KEY) = db.kVQueries.getByKey(key.name).asFlow()
            .mapToOneOrNull().map { it?.toModel() }

        fun String?.asFullScreenShowTimeOfTheDay(): Boolean = this?.toBoolean10() ?: false

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.kVQueries.getAll().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.kVQueries.upsert(
                key = j.getString(0),
                value_ = j.getString(1),
            )
        }
    }

    enum class KEY {

        DAY_START_OFFSET_SECONDS,
        EVENTS_HISTORY,
        FULLSCREEN_SHOW_TIME_OF_THE_DAY;

        fun getFromDIOrNull(): String? = DI.kv.firstOrNull { it.key == this.name }?.value

        fun getOrNullFlow() = db.kVQueries.getByKey(this.name).asFlow()
            .mapToOneOrNull().map { it?.toModel() }

        suspend fun upsert(value: String): Unit = dbIO {
            db.kVQueries.upsert(key = name, value_ = value)
        }
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = key

    override fun backupable__backup(): JsonElement = listOf(
        key, value
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.kVQueries.upsert(
            key = j.getString(0),
            value_ = j.getString(1),
        )
    }

    override fun backupable__delete() {
        db.kVQueries.delByKey(key)
    }
}

private fun KVSQ.toModel() = KVModel(
    key = key, value = value_
)
