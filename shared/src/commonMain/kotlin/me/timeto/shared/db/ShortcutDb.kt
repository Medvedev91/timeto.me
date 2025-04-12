package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ShortcutSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.ui.UiException
import kotlin.coroutines.cancellation.CancellationException

data class ShortcutDb(
    val id: Int,
    val name: String,
    val uri: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val ANDROID_PACKAGE_PREFIX = "app://"

        fun anyChangeFlow(): Flow<*> =
            db.shortcutQueries.anyChange().asFlow()

        suspend fun getCount(): Int = dbIo {
            db.shortcutQueries.selectCount().executeAsOne().toInt()
        }

        suspend fun selectAsc(): List<ShortcutDb> = dbIo {
            db.shortcutQueries.selectAsc().asList { toDb() }
        }

        fun selectAscFlow(): Flow<List<ShortcutDb>> =
            db.shortcutQueries.selectAsc().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            name: String,
            uri: String,
        ): ShortcutDb = dbIo {
            db.transactionWithResult {
                val newId: Int =
                    db.shortcutQueries.selectAsc().asList { toDb() }.lastOrNull()?.id?.plus(1) ?: 0
                val sqModel = ShortcutSQ(
                    id = newId,
                    name = validateNameRaw(name, exIds = setOf()),
                    uri = validateUriRaw(uri),
                )
                db.shortcutQueries.insert(sqModel)
                sqModel.toDb()
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.shortcutQueries.selectAsc().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.shortcutQueries.insert(
                ShortcutSQ(
                    id = j.getInt(0),
                    name = j.getString(1),
                    uri = j.getString(2),
                )
            )
        }
    }

    fun performUi() {
        launchExDefault {
            uiShortcutFlow.emit(this@ShortcutDb)
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        name: String,
        uri: String,
    ): ShortcutDb = dbIo {
        db.transactionWithResult {
            val validatedName: String =
                validateNameRaw(name, setOf(id))
            val validatedUri: String =
                validateUriRaw(uri)
            db.shortcutQueries.updateById(
                id = id,
                name = validatedName,
                uri = validatedUri,
            )
            this@ShortcutDb.copy(
                name = validatedName,
                uri = validatedUri,
            )
        }
    }

    suspend fun delete(): Unit = dbIo {
        db.shortcutQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, uri
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.shortcutQueries.updateById(
            id = j.getInt(0),
            name = j.getString(1),
            uri = j.getString(2),
        )
    }

    override fun backupable__delete() {
        db.shortcutQueries.deleteById(id)
    }
}

///

@Throws(UiException::class)
private fun validateNameRaw(
    name: String,
    exIds: Set<Int>,
): String {

    val validatedName: String = name.trim()
    if (validatedName.isEmpty())
        throw UiException("Empty name")

    db.shortcutQueries.selectAsc()
        .asList { toDb() }
        .filter { it.id !in exIds }
        .forEach { shortcut ->
            if (shortcut.name.equals(validatedName, ignoreCase = true))
                throw UiException("$validatedName already exists")
        }

    return validatedName
}

@Throws(UiException::class)
private fun validateUriRaw(uri: String): String {
    val validatedUri: String = uri.trim()
    if (validatedUri.isEmpty())
        throw UiException("Empty shortcut link")
    return validatedUri
}

///

private fun ShortcutSQ.toDb() = ShortcutDb(
    id = id, name = name, uri = uri,
)
