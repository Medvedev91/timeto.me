package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.ChecklistSQ
import kotlinx.coroutines.flow.map
import timeto.shared.UIException
import timeto.shared.time

data class ChecklistModel(
    val id: Int,
    val name: String,
) {

    companion object {

        suspend fun getAsc() = dbIO {
            db.checklistQueries.getAsc().executeAsList().map { it.toModel() }
        }

        fun getAscFlow() = db.checklistQueries.getAsc().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun addWithValidation(
            name: String,
        ) {
            val nextId = time()
            addRaw(
                id = nextId,
                name = validateName(name),
            )
        }

        fun addRaw(
            id: Int,
            name: String,
        ) {
            db.checklistQueries.insert(
                id = id, name = name
            )
        }

        fun truncate() {
            db.checklistQueries.truncate()
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
                .forEach { checklist ->
                    if (checklist.name.equals(validatedName, ignoreCase = true))
                        throw UIException("$validatedName already exists.")
                }

            return validatedName
        }

        private fun ChecklistSQ.toModel() = ChecklistModel(
            id = id, name = name
        )
    }

    suspend fun upNameWithValidation(newName: String): Unit = dbIO {
        db.checklistQueries.upNameById(
            id = id, name = validateName(newName, setOf(id))
        )
    }

    suspend fun deleteWithDependencies(): Unit = dbIO {
        ChecklistItemModel.getAsc().filter { it.list_id == id }.forEach { it.delete() }
        db.checklistQueries.deleteById(id)
    }
}
