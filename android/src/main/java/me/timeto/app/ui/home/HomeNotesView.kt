package me.timeto.app.ui.home

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.MainActivity
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight
import me.timeto.app.ui.notes.NotesView
import me.timeto.shared.db.NoteFolderDb

@Composable
fun ColumnScope.HomeNotesView(
    noteFolderDb: NoteFolderDb,
) {

    val scrollState = rememberLazyListState()
    val mainActivity = LocalActivity.current as MainActivity

    Text(
        text = noteFolderDb.name,
        modifier = Modifier
            .padding(
                top = mainActivity.statusBarHeightDp,
                start = HomeScreen__hPadding,
                bottom = 8.dp,
            ),
        fontSize = Header__titleFontSize,
        fontWeight = Header__titleFontWeight,
        color = c.text,
    )

    NotesView(
        noteFolderDb = noteFolderDb,
        hPadding = HomeScreen__hPadding,
        scrollState = scrollState,
        withDivider = false,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    )
}
