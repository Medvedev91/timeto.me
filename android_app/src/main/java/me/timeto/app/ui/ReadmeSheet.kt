package me.timeto.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

                    is ReadmeSheetVM.Paragraph.ChartImages -> {

                        HStack(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = imagesHBlock),
                        ) {
                            ChartImageView(R.drawable.readme_chart_1)
                            ChartImageView(R.drawable.readme_chart_2)
                            ChartImageView(R.drawable.readme_chart_3)
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

private val imageBorderColor = ColorRgba(96, 96, 96).toColor()

@Composable
private fun RowScope.ChartImageView(
    @DrawableRes resId: Int?,
) {

    if (resId == null) {
        SpacerW1()
        return
    }

    Image(
        painter = painterResource(resId),
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = imagesHBetween)
            .clip(imagesShape)
            .border(1.dp, imageBorderColor, shape = imagesShape)
            .clickable {
                Sheet.show { layer ->

                    VStack(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(c.sheetBg),
                    ) {

                        Image(
                            painter = painterResource(resId),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentDescription = "Chart Screenshot",
                            contentScale = ContentScale.Fit,
                        )

                        Sheet__BottomViewClose {
                            layer.close()
                        }
                    }
                }
            },
        contentDescription = "Chart Screenshot",
        contentScale = ContentScale.Fit,
    )
}
