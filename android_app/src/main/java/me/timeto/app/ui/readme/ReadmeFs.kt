package me.timeto.app.ui.readme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.app.ui.DividerBg
import me.timeto.app.ui.Padding
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.Navigation
import me.timeto.shared.ui.readme.ReadmeVm
import me.timeto.shared.ui.readme.ReadmeVm.DefaultItem

private val imagesHBetween = 4.dp
private val imagesHBlock = H_PADDING - imagesHBetween
private val imagesShape = SquircleShape(10.dp)

private val pTextLineHeight = 23.sp

@Composable
fun ReadmeFs(
    defaultItem: DefaultItem = DefaultItem.basics,
) {
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ReadmeVm(defaultItem = defaultItem)
    }

    VStack(
        modifier = Modifier
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LaunchedEffect(state.tabUi.id) {
            scrollState.scrollTo(0)
        }

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .weight(1f),
        ) {

            val paragraphs = state.tabUi.paragraphs

            paragraphs.forEachIndexed { idx, paragraph ->

                val prevP: ReadmeVm.Paragraph? =
                    if (idx == 0) null else paragraphs[idx - 1]

                when (paragraph) {

                    is ReadmeVm.Paragraph.Title -> PTitleView(paragraph.text, prevP)

                    is ReadmeVm.Paragraph.Text -> PTextView(paragraph.text, prevP)

                    is ReadmeVm.Paragraph.TextHighlight -> PTextHighlightView(paragraph.text, prevP)

                    is ReadmeVm.Paragraph.AskAQuestion -> {

                        FormButton(
                            title = paragraph.title,
                            isFirst = true,
                            isLast = true,
                            modifier = Modifier
                                .padding(top = 20.dp),
                            onClick = {
                                askAQuestion(subject = paragraph.subject)
                            },
                        )
                    }

                    is ReadmeVm.Paragraph.TimerTypical -> {
                        ImagePreviewsView(
                            R.drawable.readme_timer_1,
                        )
                    }

                    is ReadmeVm.Paragraph.TimerMyActivities -> {
                        ImagePreviewsView(
                            R.drawable.readme_activities_1,
                        )
                    }

                    is ReadmeVm.Paragraph.TimerCharts -> {
                        ImagePreviewsView(
                            R.drawable.readme_chart_1,
                            R.drawable.readme_chart_2,
                            R.drawable.readme_chart_3,
                        )
                    }

                    is ReadmeVm.Paragraph.TimerPractice1 -> {
                        ImagePreviewsView(
                            R.drawable.readme_timer_practice_1,
                            R.drawable.readme_timer_practice_2,
                            R.drawable.readme_timer_practice_3,
                            R.drawable.readme_timer_practice_4,
                        )
                    }

                    is ReadmeVm.Paragraph.TimerPractice2 -> {
                        ImagePreviewsView(
                            R.drawable.readme_timer_practice_5,
                            R.drawable.readme_chart_2,
                            R.drawable.readme_chart_3,
                        )
                    }

                    is ReadmeVm.Paragraph.RepeatingsMy -> {
                        ImagePreviewsView(
                            R.drawable.readme_repeatings_1,
                        )
                    }


                    is ReadmeVm.Paragraph.RepeatingsToday -> {
                        ImagePreviewsView(
                            R.drawable.readme_repeatings_2,
                        )
                    }


                    is ReadmeVm.Paragraph.RepeatingsPractice1 -> {
                        ImagePreviewsView(
                            R.drawable.readme_repeating_practice_1,
                            R.drawable.readme_repeating_practice_2,
                            R.drawable.readme_repeating_practice_3,
                        )
                    }


                    is ReadmeVm.Paragraph.RepeatingsPractice2 -> {
                        ImagePreviewsView(
                            R.drawable.readme_repeating_practice_4,
                            R.drawable.readme_repeating_practice_5,
                        )
                    }

                    is ReadmeVm.Paragraph.ChecklistsExamples -> {
                        ImagePreviewsView(
                            R.drawable.readme_checklists_1,
                            R.drawable.readme_checklists_2,
                            R.drawable.readme_checklists_3,
                        )
                    }

                    is ReadmeVm.Paragraph.ChecklistsPractice1 -> {
                        ImagePreviewsView(
                            R.drawable.readme_checklists_practice_1,
                            R.drawable.readme_checklists_practice_2,
                            R.drawable.readme_checklists_practice_3,
                            R.drawable.readme_checklists_practice_4,
                            R.drawable.readme_checklists_practice_5,
                            R.drawable.readme_checklists_practice_6,
                            R.drawable.readme_checklists_practice_7,
                        )
                    }

                    is ReadmeVm.Paragraph.ChecklistsPractice2 -> {
                        ImagePreviewsView(
                            R.drawable.readme_checklists_practice_8,
                            R.drawable.readme_checklists_practice_9,
                        )
                    }

                    is ReadmeVm.Paragraph.PomodoroExamples -> {
                        ImagePreviewsView(
                            R.drawable.readme_pomodoro_1,
                            R.drawable.readme_pomodoro_2,
                            R.drawable.readme_pomodoro_3,
                        )
                    }

                    is ReadmeVm.Paragraph.GoalsExamples -> {
                        ImagePreviewsView(
                            R.drawable.readme_goals_1,
                        )
                    }

                    is ReadmeVm.Paragraph.CalendarExamples -> {
                        ImagePreviewsView(
                            R.drawable.readme_calendar_1,
                            R.drawable.readme_calendar_2,
                        )
                    }
                }
            }

            Padding(vertical = 26.dp)
        }

        DividerBg(Modifier.padding(horizontal = H_PADDING))

        HStack(
            modifier = Modifier
                .padding(top = 9.dp, bottom = 4.dp)
                .padding(horizontal = H_PADDING)
                .navigationBarsPadding(),
        ) {

            state.tabsUi.forEach { tabUi ->
                TabBarItemView(
                    title = tabUi.title,
                    isActive = state.tabUi.id == tabUi.id,
                    onClick = {
                        vm.setTabUi(tabUi)
                    },
                )
                Padding(horizontal = 6.dp)
            }
        }
    }
}

///

private val tabBarItemViewShape = SquircleShape(10.dp)

@Composable
private fun TabBarItemView(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val bgColorAnimate =
        animateColorAsState(if (isActive) c.blue else c.transparent)
    Text(
        text = title,
        modifier = Modifier
            .clip(tabBarItemViewShape)
            .background(bgColorAnimate.value)
            .clickable {
                onClick()
            }
            .padding(horizontal = 8.dp)
            .padding(top = 1.dp, bottom = halfDpCeil),
        color = if (isActive) c.white else c.text,
        fontSize = 14.sp,
        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
    )
}

///

@Composable
private fun PTitleView(
    text: String,
    prevP: ReadmeVm.Paragraph?,
) {
    val paddingTop: Dp = when {
        prevP == null -> 14.dp
        prevP.isSlider -> 36.dp
        prevP is ReadmeVm.Paragraph.Text -> 38.dp
        else -> throw Exception()
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(top = paddingTop),
        color = c.text,
        fontWeight = Header__titleFontWeight,
        fontSize = Header__titleFontSize,
    )
}

@Composable
private fun PTextView(
    text: String,
    prevP: ReadmeVm.Paragraph?,
) {
    val paddingTop: Dp = when {
        prevP == null -> 13.dp
        prevP.isSlider -> 10.dp
        prevP is ReadmeVm.Paragraph.Title -> 15.dp
        prevP is ReadmeVm.Paragraph.Text -> 12.dp // Text height * 3
        prevP is ReadmeVm.Paragraph.TextHighlight -> 18.dp // Equals to paragraph padding
        else -> throw Exception()
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(top = paddingTop),
        color = c.text,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun PTextHighlightView(
    text: String,
    prevP: ReadmeVm.Paragraph?,
) {
    val paddingTop: Dp = when {
        prevP == null -> throw Exception()
        prevP.isSlider -> 18.dp
        prevP is ReadmeVm.Paragraph.Text -> 20.dp
        else -> throw Exception()
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingTop)
            .padding(horizontal = H_PADDING - 2.dp)
            .clip(squircleShape)
            .background(c.blue)
            .padding(horizontal = 14.dp)
            .padding(top = 11.dp, bottom = 11.dp),
        color = c.text,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

private val imageBorderColor = c.dividerBg
private val imageSliderShape = SquircleShape(24.dp)

@Composable
private fun ImagePreviewsView(
    vararg resIds: Int,
) {

    val navigationFs = LocalNavigationFs.current
    val scrollState = rememberScrollState()

    HStack(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 8.dp)
            .horizontalScroll(scrollState),
    ) {

        Padding(horizontal = imagesHBlock)

        resIds.forEach { resId ->

            Image(
                painter = painterResource(resId),
                modifier = Modifier
                    .height(250.dp)
                    .padding(horizontal = imagesHBetween)
                    .clip(imagesShape)
                    .border(1.dp, imageBorderColor, shape = imagesShape)
                    .clickable {
                        showImagesSlider(
                            navigationFs = navigationFs,
                            resIds = resIds,
                        )
                    },
                contentDescription = "Screenshot",
                contentScale = ContentScale.Fit,
            )
        }

        Padding(horizontal = imagesHBlock)
    }
}

private fun showImagesSlider(
    navigationFs: Navigation,
    vararg resIds: Int,
) {
    navigationFs.push {
        val navigationLayer = LocalNavigationLayer.current

        VStack(
            modifier = Modifier
                .background(c.bg)
                .fillMaxSize()
                .padding(top = (LocalContext.current as MainActivity).statusBarHeightDp + 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val scrollState = rememberScrollState()
            HStack(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
            ) {

                Padding(horizontal = 8.dp)

                resIds.forEach { resId ->
                    Image(
                        painter = painterResource(resId),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxHeight()
                            .clip(imageSliderShape)
                            .border(1.dp, imageBorderColor, shape = imageSliderShape),
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.FillHeight,
                    )
                }

                Padding(horizontal = 8.dp)
            }

            Text(
                text = "Close",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .padding(vertical = 8.dp)
                    .align(Alignment.End)
                    .navigationBarsPadding()
                    .clip(roundedShape)
                    .clickable {
                        navigationLayer.close()
                    }
                    .padding(horizontal = 14.dp)
                    .padding(top = 6.dp, bottom = 7.dp),
                color = c.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
            )
        }
    }
}
