package me.timeto.app.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.ReadmeSheetVM

private val hPadding = MyListView.PADDING_OUTER_HORIZONTAL

private val imagesHBetween = 4.dp
private val imagesHBlock = 10.dp
private val imagesShape = SquircleShape(len = 50f)

private val pTextLineHeight = 22.sp

@Composable
fun ReadmeSheet(
    layer: WrapperView.Layer,
) {

    val (_, state) = rememberVM { ReadmeSheetVM() }

    VStack(
        modifier = Modifier
            .background(c.sheetBg)
    ) {

        val scrollState = rememberScrollState()

        Sheet__HeaderView(
            title = state.title,
            scrollState = scrollState,
            bgColor = c.sheetBg,
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .weight(1f),
        ) {

            state.paragraphs.forEach { paragraph ->

                when (paragraph) {

                    is ReadmeSheetVM.Paragraph.Title -> PTitleView(paragraph.text)

                    is ReadmeSheetVM.Paragraph.Text -> PTextView(paragraph.text)

                    is ReadmeSheetVM.Paragraph.RedText -> PRedTextView(paragraph.text)

                    is ReadmeSheetVM.Paragraph.ListDash -> PListDashedView(paragraph.items)

                    is ReadmeSheetVM.Paragraph.ChartImages -> {

                        HStack(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = imagesHBlock),
                        ) {
                            ImagePreviewsView(
                                R.drawable.readme_chart_1,
                                R.drawable.readme_chart_2,
                                R.drawable.readme_chart_3,
                            )
                        }
                    }

                    is ReadmeSheetVM.Paragraph.ActivitiesImage -> {

                        HStack(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = imagesHBlock),
                        ) {
                            ImagePreviewsView(
                                R.drawable.readme_activities_1,
                            )
                        }
                    }

                    is ReadmeSheetVM.Paragraph.AskAQuestion -> {

                        MyListView__ItemView(
                            isFirst = true,
                            isLast = true,
                            modifier = Modifier
                                .padding(top = 24.dp),
                        ) {
                            MyListView__ItemView__ButtonView(text = paragraph.title) {
                                askAQuestion(subject = paragraph.subject)
                            }
                        }
                    }
                }
            }
        }

        Sheet__BottomViewClose {
            layer.close()
        }
    }
}

@Composable
private fun PTitleView(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPadding)
            .padding(top = 48.dp),
        color = c.white,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    )
}

@Composable
private fun PTextView(
    text: String,
    topPadding: Dp = 16.dp,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPadding)
            .padding(top = topPadding),
        color = c.white,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun PRedTextView(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(c.red)
            .padding(horizontal = hPadding)
            .padding(top = 12.dp, bottom = 10.dp),
        color = c.white,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun PListDashedView(
    items: List<String>,
) {
    VStack {

        items.forEach { item ->

            HStack(
                modifier = Modifier
                    .padding(top = 8.dp),
            ) {

                Icon(
                    painter = painterResource(R.drawable.sf_minus_medium_regular),
                    contentDescription = item,
                    modifier = Modifier
                        .padding(start = hPadding, top = 5.dp)
                        .size(12.dp),
                    tint = c.white,
                )

                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = hPadding),
                    color = c.white,
                    lineHeight = pTextLineHeight,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

private val imageBorderColor = ColorRgba(96, 96, 96).toColor()
private val imageSliderShape = SquircleShape(len = 90f, angleParam = 10f)

private val imageSliderEnterAnimation: EnterTransition = slideInVertically(
    animationSpec = spring(
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    initialOffsetY = { it },
)

private val imageSliderExitAnimation: ExitTransition = slideOutVertically(
    animationSpec = spring(
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    targetOffsetY = { it },
)

@Composable
private fun ImagePreviewsView(
    vararg resIds: Int,
) {
    val scrollState = rememberScrollState()
    HStack(
        modifier = Modifier
            .horizontalScroll(scrollState),
    ) {

        resIds.forEach { resId ->

            Image(
                painter = painterResource(resId),
                modifier = Modifier
                    .height(250.dp)
                    .padding(horizontal = imagesHBetween)
                    .clip(imagesShape)
                    .border(1.dp, imageBorderColor, shape = imagesShape)
                    .clickable {
                        showImagesSlider(*resIds)
                    },
                contentDescription = "Screenshot",
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun showImagesSlider(
    vararg resIds: Int,
) {

    WrapperView.Layer(
        enterAnimation = imageSliderEnterAnimation,
        exitAnimation = imageSliderExitAnimation,
        alignment = Alignment.BottomCenter,
        onClose = {},
        content = { layer ->
            VStack(
                modifier = Modifier
                    .background(c.black)
                    .fillMaxHeight()
                    .padding(top = statusBarHeight)
                    .pointerInput(Unit) { },
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
                            layer.close()
                        }
                        .padding(horizontal = 14.dp)
                        .padding(top = 6.dp, bottom = 7.dp),
                    color = c.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    ).show()
}
