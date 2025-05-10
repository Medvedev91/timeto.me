package me.timeto.app.ui.navigation.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.R
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.halfDpFloor
import me.timeto.app.ui.Divider
import me.timeto.app.ui.navigation.LocalNavigationLayer

@Composable
fun <T> NavigationPicker(
    items: List<NavigationPickerItem<T>>,
    onDone: (item: NavigationPickerItem<T>) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    LazyColumn {
        items.forEachIndexed { idx, item ->
            item {

                ZStack(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .clickable {
                            onDone(item)
                            navigationLayer.close()
                        }
                        .padding(start = 10.dp),
                ) {

                    HStack(
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        ZStack(
                            modifier = Modifier
                                .offset(y = -halfDpFloor)
                                .width(20.dp),
                        ) {
                            if (item.isSelected) {
                                Icon(
                                    painterResource(R.drawable.sf_checkmark_medium_medium),
                                    contentDescription = "Selected",
                                    tint = c.white,
                                    modifier = Modifier
                                        .size(13.dp),
                                )
                            }
                        }

                        Text(
                            text = item.title,
                            color = c.white,
                        )
                    }

                    if (idx > 0) {
                        Divider(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .align(Alignment.TopStart),
                        )
                    }
                }
            }
        }
    }
}
