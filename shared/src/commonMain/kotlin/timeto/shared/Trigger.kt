package timeto.shared

import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel

sealed class Trigger(
    val id: String,
    val title: String,
    val typeSortAsc: Int,
) {

    ///
    /// https://stackoverflow.com/q/2265503/5169420

    // IDE recommends implementing hashCode() it the equals() exists,
    // According to stackoverflow it is needed for HashMap...
    override fun hashCode() = id.hashCode()

    override fun equals(
        other: Any?
    ): Boolean {
        if (other is Trigger)
            return id == other.id
        return super.equals(other)
    }

    //////

    fun getColor() = when (this) {
        is Checklist -> ColorNative.green
        is Shortcut -> ColorNative.red
    }

    fun performUI() {
        val _when = when (this) {
            is Checklist -> launchExDefault { checklist.performUI() }
            is Shortcut -> launchExDefault { shortcut.performUI() }
        }
    }

    class Checklist(
        val checklist: ChecklistModel
    ) : Trigger("#c${checklist.id}", checklist.name, 1)

    class Shortcut(
        val shortcut: ShortcutModel
    ) : Trigger("#s${shortcut.id}", shortcut.name, 2)
}
