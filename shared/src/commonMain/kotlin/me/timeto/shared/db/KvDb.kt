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
import me.timeto.shared.utils.toBoolean10

data class KvDb(
    val key: String,
    val value: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAll(): List<KvDb> = dbIo {
            db.kVQueries.selectAll().asList { toDb() }
        }

        fun selectAllFlow(): Flow<List<KvDb>> =
            db.kVQueries.selectAll().asListFlow { toDb() }

        ///

        suspend fun selectTokenOrNullSafe(): String? = try {
            KEY.TOKEN.selectStringOrNull()
        } catch (e: Throwable) {
            // todo fallback report
            zlog("KvDb.selectTokenOrNullSafe():$e")
            null
        }

        ///

        fun KvDb?.asDayStartOffsetSeconds(): Int =
            this?.value?.toInt() ?: 0

        fun KvDb?.isSendingReports(): Boolean {
            val time: Int = this?.value?.toInt() ?: return !deviceData.isFdroid
            return time > 0
        }

        fun KvDb?.todayOnHomeScreen(): Boolean =
            this?.value?.toBoolean10() ?: true

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.kVQueries.selectAll().asList { toDb() }

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

        // selectOrNull..

        suspend fun selectOrNull(): KvDb? = dbIo {
            db.kVQueries.selectByKey(name).executeAsOneOrNull()?.toDb()
        }

        fun selectOrNullFlow(): Flow<KvDb?> = db.kVQueries.selectByKey(name)
            .asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDb() }

        fun selectOrNullCached(): KvDb? =
            Cache.kvDb.firstOrNull { it.key == name }

        // selectStringOrNull..

        suspend fun selectStringOrNull(): String? =
            selectOrNull()?.value

        fun selectStringOrNullFlow(): Flow<String?> =
            selectOrNullFlow().map { it?.value }

        fun selectStringOrNullCached(): String? =
            selectOrNullCached()?.value

        ///

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
        key, value,
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

///

private fun KVSQ.toDb() = KvDb(
    key = key, value = value_,
)
