package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dbsq.KVSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class KvDb(
    val key: String,
    val value: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAll(): List<KvDb> = dbIo {
            db.kVQueries.selectAll().executeAsList().map { it.toDb() }
        }

        fun selectAllFlow(): Flow<List<KvDb>> = db.kVQueries.selectAll().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        ///

        suspend fun selectTokenOrNullSafe(): String? = try {
            KEY.TOKEN.selectStringOrNull()
        } catch (e: Throwable) {
            // todo fallback report
            zlog("KvDb.selectTokenOrNullSafe():$e")
            null
        }

        //

        fun String?.asDayStartOffsetSeconds(): Int = this?.toInt() ?: 0

        fun String?.isSendingReports(): Boolean {
            val time: Int = this?.toInt() ?: return !deviceData.isFdroid
            return time > 0
        }

        //

        const val TODAY_ON_HOME_SCREEN_DEFAULT = true

        fun todayOnHomeScreenCached(): Boolean =
            KEY.TODAY_ON_HOME_SCREEN.selectBooleanOrNullCached()
            ?: TODAY_ON_HOME_SCREEN_DEFAULT

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.kVQueries.selectAll().executeAsList().map { it.toDb() }

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
        WHATS_NEW_CHECK_UNIX_DAY,
        FEEDBACK_SUBJECT,
        TODAY_ON_HOME_SCREEN,
        IS_SENDING_REPORTS,
        HOME_README_OPEN_TIME;

        suspend fun selectOrNull(): String? =
            selectAll().firstOrNull { it.key == this.name }?.value

        suspend fun selectStringOrNull(): String? =
            selectAll().firstOrNull { it.key == this.name }?.value

        fun selectStringOrNullCached(): String? =
            Cache.kvDb.firstOrNull { it.key == this.name }?.value

        fun selectBooleanOrNullCached(): Boolean? =
            selectStringOrNullCached()?.toBoolean10()

        fun selectStringOrNullFlow(): Flow<String?> = db.kVQueries
            .selectByKey(this.name)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.value_ }

        fun selectBooleanOrNullFlow(): Flow<Boolean?> =
            selectStringOrNullFlow().map { it?.toBoolean10() }

        fun getOrNullFlow(): Flow<KvDb?> = db.kVQueries.selectByKey(this.name).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it?.toDb() }

        suspend fun upsert(value: String?): Unit = dbIo {
            if (value == null)
                db.kVQueries.deleteByKey(key = name)
            else
                db.kVQueries.upsert(key = name, value_ = value)
        }

        suspend fun upsertBool(value: Boolean?): Unit = dbIo {
            val newVal: String? = when (value) {
                true -> "1"
                false -> "0"
                null -> null
            }
            upsert(newVal)
        }

        suspend fun upsertInt(value: Int?): Unit = dbIo {
            upsert(value?.toString())
        }

        //

        suspend fun upsertIsSendingReports(isSendingReports: Boolean) {
            upsertInt(if (isSendingReports) time() else (-time()))
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
        db.kVQueries.deleteByKey(key)
    }
}

private fun KVSQ.toDb() = KvDb(
    key = key, value = value_
)

private fun String.toBool(): Boolean = when (this) {
    "1" -> true
    "0" -> false
    else -> throw Exception()
}
