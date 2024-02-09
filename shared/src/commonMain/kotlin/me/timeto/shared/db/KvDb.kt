package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dbsq.KVSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class KvDb(
    val key: String,
    val value: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun getAll(): List<KvDb> = dbIO { selectAllPlain() }

        fun selectAllPlain(): List<KvDb> =
            db.kVQueries.getAll().executeAsList().map { it.toModel() }

        fun getAllFlow() = db.kVQueries.getAll().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toModel() } }

        //

        fun String?.asDayStartOffsetSeconds(): Int = this?.toInt() ?: 0

        //
        // Backupable Holder

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
        TOKEN,
        TOKEN_PASSWORD,
        FEEDBACK_SUBJECT;

        suspend fun selectOrNull(): String? = getAll().firstOrNull { it.key == this.name }?.value

        fun getFromDIOrNull(): String? = DI.kv.firstOrNull { it.key == this.name }?.value

        fun getOrNullFlow() = db.kVQueries.getByKey(this.name).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it?.toModel() }

        suspend fun upsert(value: String): Unit = dbIO {
            db.kVQueries.upsert(key = name, value_ = value)
        }
    }

    //
    // Backupable Item

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

private fun KVSQ.toModel() = KvDb(
    key = key, value = value_
)
