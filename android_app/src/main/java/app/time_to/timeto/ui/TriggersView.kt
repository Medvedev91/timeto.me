package app.time_to.timeto.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import app.time_to.timeto.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timeto.shared.Trigger
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TriggersView__FormView(
    triggers: List<Trigger>,
    onTriggersChanged: (List<Trigger>) -> Unit,
    modifier: Modifier = Modifier,
    contentPaddingHints: PaddingValues = PaddingValues(),
    defBg: Color = c.background2
) {
    val scope = rememberCoroutineScope()

    /**
     * todo by VM state
     */
    val checklists = ChecklistModel.getAscFlow().collectAsState(emptyList()).value
    val shortcuts = ShortcutModel.getAscFlow().collectAsState(emptyList()).value

    val triggersSorted = (checklists.map { Trigger.Checklist(it) } + shortcuts.map { Trigger.Shortcut(it) })
        .sortedWith(
            compareBy(
                { trigger -> !triggers.contains(trigger) },
                { it.typeSortAsc },
                { it.id },
            )
        )

    Column {

        if (triggersSorted.isNotEmpty()) {

            val listState = rememberLazyListState()
            LazyRow(
                contentPadding = contentPaddingHints,
                state = listState,
                modifier = modifier
            ) {
                itemsIndexed(
                    triggersSorted,
                    key = { _, checklist -> checklist.id }
                ) { _, trigger ->
                    Box(
                        modifier = Modifier
                            .padding(end = if (trigger == triggersSorted.last()) 0.dp else 8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(c.blue)
                            .padding(1.dp)
                            .animateItemPlacement()
                    ) {
                        val isSelected = triggers.contains(trigger)
                        Text(
                            trigger.title,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (isSelected) c.blue else defBg)
                                .clickable {
                                    onTriggersChanged(
                                        triggers
                                            .toMutableList()
                                            .apply {
                                                if (isSelected) remove(trigger)
                                                else add(trigger)
                                            }
                                    )

                                    scope.launch {
                                        delay(200)
                                        listState.animateScrollToItem(0)
                                    }
                                }
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 5.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600,
                            color = if (isSelected) c.white else c.blue,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TriggersView__ListView(
    triggers: List<Trigger>,
    withOnClick: Boolean,
    modifier: Modifier = Modifier,
    withDeletion: ((trigger: Trigger) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues()
) {
    if (triggers.isEmpty())
        return

    val context = LocalContext.current
    val errorDialog = LocalErrorDialog.current

    val triggersDialogManager = LocalTriggersDialogManager.current

    val itemHeight = 26.dp
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            triggers,
            key = { _, checklist -> checklist.id }
        ) { _, trigger ->
            val isLast = triggers.last() == trigger
            Row(
                modifier = Modifier
                    .padding(end = if (isLast) 0.dp else 8.dp)
                    .height(itemHeight)
                    .clip(MySquircleShape(len = 50f))
                    .background(
                        trigger
                            .getColor()
                            .toColor()
                    )
                    .clickable(withOnClick) {
                        triggersDialogManager.show(trigger, context, errorDialog)
                    }
                    .padding(start = 8.dp, end = if (withDeletion != null) 1.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    trigger.title,
                    modifier = Modifier
                        .offset(y = (-0.8).dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = c.white
                )
                if (withDeletion != null)
                    Icon(
                        painterResource(id = R.drawable.ic_round_close_24),
                        "Delete",
                        modifier = Modifier
                            .size(itemHeight)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                withDeletion(trigger)
                            }
                            .padding(4.dp),
                        tint = c.white
                    )
            }
        }
    }
}

class TriggersView__DialogManager {

    val checklist = mutableStateOf<ChecklistModel?>(null)
    val checklistIsPresented = mutableStateOf(false)

    fun show(
        trigger: Trigger,
        context: Context,
        errorDialogMessage: MutableState<String?>
    ) {
        val whenRes = when (trigger) {
            is Trigger.Checklist -> {
                checklistIsPresented.value = false
                checklist.value = trigger.checklist
                checklistIsPresented.value = true
            }
            is Trigger.Shortcut -> {
                performShortcutOrError(trigger.shortcut, context, errorDialogMessage)
            }
        }
    }
}
