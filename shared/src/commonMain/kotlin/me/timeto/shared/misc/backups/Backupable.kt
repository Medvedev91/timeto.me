package me.timeto.shared.misc.backups

import kotlinx.serialization.json.JsonElement

/**
 * WARNING
 *
 * Keep in mind that the data is "reliable", i.e., it does not need to
 * be checked. If the user made changes manually - there's nothing we can do.
 *
 * The methods must be suitable for use within transactions:
 * - Do not save to DB;
 * - Do not use suspend (it crashes sqldelight transactions).
 */
interface Backupable__Item {

    fun backupable__getId(): String

    fun backupable__backup(): JsonElement

    fun backupable__update(json: JsonElement)

    fun backupable__delete()
}

interface Backupable__Holder {

    fun backupable__getAll(): List<Backupable__Item>

    fun backupable__restore(json: JsonElement)
}

