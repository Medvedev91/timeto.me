package me.timeto.shared

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import dbsq.ActivitySQ
import dbsq.ChecklistItemSQ
import dbsq.ChecklistSQ
import dbsq.EventSQ
import dbsq.EventTemplateSQ
import dbsq.GoalSq
import dbsq.IntervalSQ
import dbsq.NoteSQ
import dbsq.RepeatingSQ
import dbsq.ShortcutSQ
import dbsq.TaskFolderSQ
import dbsq.TaskSQ
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
        ActivitySQAdapter = ActivitySQ.Adapter(
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
        IntervalSQAdapter = IntervalSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        RepeatingSQAdapter = RepeatingSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ShortcutSQAdapter = ShortcutSQ.Adapter(
            IntColumnAdapter,
        ),
        TaskFolderSQAdapter = TaskFolderSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        TaskSQAdapter = TaskSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        NoteSQAdapter = NoteSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        GoalSqAdapter = GoalSq.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
    )
    SystemInfo.instance = systemInfo
    initKmpDeferred = ioScope().async {
        Cache.init()
    }
}
