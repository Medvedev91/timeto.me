package me.timeto.app.ui.symbol

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import me.timeto.app.R
import me.timeto.shared.Symbol

@Composable
fun SymbolIconView(
    icon: Symbol.Icon,
    color: Color,
    size: Dp,
    modifier: Modifier,
) {
    Icon(
        painter = painterResource(id = icon.iconResId()),
        contentDescription = icon.iconEnum.name,
        modifier = modifier
            .size(size),
        tint = color,
    )
}

@DrawableRes
private fun Symbol.Icon.iconResId(): Int = when (iconEnum) {
    Symbol.Icon.IconEnum.book -> R.drawable.ms_book_2_fill
    Symbol.Icon.IconEnum.case -> R.drawable.ms_work_fill
    Symbol.Icon.IconEnum.timer -> R.drawable.ms_timer_fill
    Symbol.Icon.IconEnum.exercise -> R.drawable.ms_exercise_fill
    Symbol.Icon.IconEnum.piano -> R.drawable.ms_piano
    Symbol.Icon.IconEnum.music_note -> R.drawable.ms_music_note
    Symbol.Icon.IconEnum.option -> R.drawable.ms_keyboard_option_key_w700
    Symbol.Icon.IconEnum.graduationcap -> R.drawable.ms_school_fill
    Symbol.Icon.IconEnum.megaphone -> R.drawable.ms_campaign_fill_w700
    Symbol.Icon.IconEnum.instruments -> R.drawable.ms_construction_fill
    Symbol.Icon.IconEnum.meditation -> R.drawable.ms_self_improvement_fill_w700
    Symbol.Icon.IconEnum.rocket -> R.drawable.ms_rocket_launch_fill
    Symbol.Icon.IconEnum.flask -> R.drawable.ms_science_fill
    Symbol.Icon.IconEnum.compass -> R.drawable.ms_architecture_fill_w700
    Symbol.Icon.IconEnum.gamecontroller -> R.drawable.ms_sports_esports_fill
    Symbol.Icon.IconEnum.soccerball -> R.drawable.ms_sports_soccer_fill
    Symbol.Icon.IconEnum.hiking -> R.drawable.ms_hiking_fill
    Symbol.Icon.IconEnum.bus -> R.drawable.ms_directions_bus_fill
    Symbol.Icon.IconEnum.bulb -> R.drawable.ms_lightbulb_fill
    Symbol.Icon.IconEnum.bolt -> R.drawable.ms_bolt_fill
    Symbol.Icon.IconEnum.inbox -> R.drawable.ms_inbox_fill
    Symbol.Icon.IconEnum.sun -> R.drawable.ms_wb_sunny_fill
    Symbol.Icon.IconEnum.moon -> R.drawable.ms_bedtime_fill
    Symbol.Icon.IconEnum.moon_stars -> R.drawable.ms_moon_stars_fill
    Symbol.Icon.IconEnum.film -> R.drawable.ms_theaters_fill
    Symbol.Icon.IconEnum.coffee -> R.drawable.ms_coffee_fill
    Symbol.Icon.IconEnum.tennis -> R.drawable.ms_sports_tennis_fill
    Symbol.Icon.IconEnum.surfing -> R.drawable.ms_surfing_fill
    Symbol.Icon.IconEnum.skiing -> R.drawable.ms_downhill_skiing_fill
    Symbol.Icon.IconEnum.fork_knife -> R.drawable.ms_restaurant_fill
    Symbol.Icon.IconEnum.hockey -> R.drawable.ms_sports_hockey_fill
    Symbol.Icon.IconEnum.pencil_note -> R.drawable.ms_edit_note_fill
    Symbol.Icon.IconEnum.question -> R.drawable.ms_question_mark
}
