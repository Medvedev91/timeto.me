package me.timeto.shared

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import dbsq.ActivitySq
import dbsq.ChecklistItemSQ
import dbsq.ChecklistSQ
import dbsq.EventSQ
import dbsq.EventTemplateSQ
import dbsq.IntervalSq
import dbsq.NoteSQ
import dbsq.RepeatingSQ
import dbsq.ShortcutSQ
import dbsq.TaskFolderSq
import dbsq.TaskSq
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.db

lateinit var initKmpDeferred: Deferred<Unit>

internal fun initKmp(
    sqlDriver: SqlDriver,
    systemInfo: SystemInfo,
) {
    db = TimetomeDB(
        driver = sqlDriver,
        ActivitySqAdapter = ActivitySq.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ChecklistItemSQAdapter = ChecklistItemSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ChecklistSQAdapter = ChecklistSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        EventSQAdapter = EventSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        EventTemplateSQAdapter = EventTemplateSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        IntervalSqAdapter = IntervalSq.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        RepeatingSQAdapter = RepeatingSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ShortcutSQAdapter = ShortcutSQ.Adapter(
            IntColumnAdapter,
        ),
        TaskFolderSqAdapter = TaskFolderSq.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        TaskSqAdapter = TaskSq.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        NoteSQAdapter = NoteSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
    )
    SystemInfo.instance = systemInfo
    initKmpDeferred = ioScope().async {
        Cache.init()
    }
}
