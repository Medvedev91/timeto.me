package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.ShortcutSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import timeto.shared.*
import kotlin.math.max

data class ShortcutModel(
    val id: Int,
    val name: String,
    val uri: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.shortcutQueries.anyChange().asFlow()

        suspend fun getCount(): Int = dbIO {
            db.shortcutQueries.getCount().executeAsOne().toInt()
        }

        suspend fun getAsc() = dbIO {
            db.shortcutQueries.getAsc().executeAsList().map { it.toModel() }
        }

        fun getAscFlow() = db.shortcutQueries.getAsc().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun getByIdOrNull(id: Int) = dbIO {
            db.shortcutQueries.getById(id).executeAsOneOrNull()?.toModel()
        }

        suspend fun addWithValidation(
            name: String,
            uri: String,
        ) = dbIO {
            val validatedName = validateName(name) // todo to inside transaction
            db.transaction {
                addRaw(
                    id = max(time(), db.shortcutQueries.getDesc(1).executeAsOneOrNull()?.id?.plus(1) ?: 0),
                    name = validatedName,
                    uri = validateUri(uri)
                )
            }
        }

        fun addRaw(
            id: Int,
            name: String,
            uri: String,
        ) {
            db.shortcutQueries.insert(
                id = id, name = name, uri = uri
            )
        }

        private suspend fun validateName(
            name: String,
            exIds: Set<Int> = setOf(),
        ): String {

            val validatedName = name.trim()
            if (validatedName.isEmpty())
                throw UIException("Empty name")

            getAsc()
                .filter { it.id !in exIds }
                .forEach { shortcut ->
                    if (shortcut.name.equals(validatedName, ignoreCase = true))
                        throw UIException("$validatedName already exists.")
                }

            return validatedName
        }

        private fun validateUri(uri: String): String {
            val validatedUri = uri.trim()
            if (validatedUri.isEmpty())
                throw UIException("Empty shortcut link")

            // https://stackoverflow.com/a/62856745/5169420
            // Since API level 30 resolveActivity always returns null
            // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            // if (intent.resolveActivity(context.packageManager) == null)
            //    throw MyException("Invalid action link")

            return validatedUri
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.shortcutQueries.getAsc().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.shortcutQueries.insert(
                id = j.getInt(0),
                name = j.getString(1),
                uri = j.getString(2),
            )
        }
    }

    fun performUI() {
        launchExDefault { uiShortcutFlow.emit(this@ShortcutModel) }
    }

    suspend fun upWithValidation(name: String, uri: String) = dbIO {
        db.shortcutQueries.updateById(
            id = id, name = validateName(name, setOf(id)), uri = validateUri(uri)
        )
    }

    suspend fun delete() = dbIO { db.shortcutQueries.deleteById(id) }

    ///
    /// Backupable Item

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

private fun ShortcutSQ.toModel() = ShortcutModel(
    id = id, name = name, uri = uri
)
