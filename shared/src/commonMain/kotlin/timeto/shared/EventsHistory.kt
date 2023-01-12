package timeto.shared

import kotlinx.serialization.json.*
import timeto.shared.db.EventModel
import timeto.shared.db.KVModel

data class EventsHistory(
    val items: MutableList<Item>
) {

    data class Item(
        val normalized_title: String, // as id
        var raw_title: String,
        var daytime: Int, // since 00:00
        var uptime: Int, // timestamp
    ) {

        companion object {

            fun fromJsonObject(j: JsonObject) = Item(
                normalized_title = j.getString("normalized_title"),
                raw_title = j.getString("raw_title"),
                daytime = j.getInt("daytime"),
                uptime = j.getInt("uptime"),
            )
        }

        fun toJsonObject() = JsonObject(
            mapOf(
                "normalized_title" to JsonPrimitive(normalized_title),
                "raw_title" to JsonPrimitive(raw_title),
                "daytime" to JsonPrimitive(daytime),
                "uptime" to JsonPrimitive(uptime),
            )
        )
    }

    suspend fun save() {
        val map = mapOf(
            "items" to JsonArray(items.map { it.toJsonObject() })
        )
        KVModel.upsert(KVModel.KEY.EVENTS_HISTORY, JsonObject(map).toString())
    }

    companion object {

        fun buildFromDI(): EventsHistory = buildFromJString(
            DI.kv.firstOrNull { it.key == KVModel.KEY.EVENTS_HISTORY.name }?.value ?: ""
        )

        fun buildFromJString(jString: String): EventsHistory {
            return try {
                // Check for validity
                val jHistory = Json.parseToJsonElement(jString)
                val items = jHistory.jsonObject["items"]!!.jsonArray.map {
                    Item.fromJsonObject(it.jsonObject)
                }
                EventsHistory(items = items.toMutableList())
            } catch (e: Throwable) {
                EventsHistory(items = mutableListOf())
            }
        }

        suspend fun upsert(
            event: EventModel
        ) {
            val normalizedTitle = normalizeTitle(event.text)
            val dayTime = event.utc_time % 86_400

            val history = buildFromDI()
            /// Remove old
            history.items.retainAll { (it.uptime + (365 * 86_400)) > time() }

            val item = history.items.firstOrNull { it.normalized_title == normalizedTitle }
            if (item != null) {
                item.raw_title = event.text
                item.daytime = dayTime
                item.uptime = time()
            } else {
                history.items.add(
                    Item(
                        normalized_title = normalizedTitle,
                        raw_title = event.text,
                        daytime = dayTime,
                        uptime = time()
                    )
                )
            }
            history.save()
        }

        suspend fun delete(itemToDelete: Item) {
            val history = buildFromDI()
            history.items.retainAll {
                it.normalized_title != itemToDelete.normalized_title
            }
            history.save()
        }

        private fun normalizeTitle(title: String): String =
            TextFeatures.parse(title).textNoFeatures.lowercase().trim()
    }
}
