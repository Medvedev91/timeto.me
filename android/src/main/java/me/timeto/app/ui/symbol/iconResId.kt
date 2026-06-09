package me.timeto.app.ui.symbol

import androidx.annotation.DrawableRes
import me.timeto.app.R
import me.timeto.shared.Symbol

@DrawableRes
fun Symbol.Icon.iconResId(): Int = when (iconEnum) {
    Symbol.Icon.IconEnum.book -> R.drawable.ms_book_2_fill
    Symbol.Icon.IconEnum.case -> R.drawable.ms_work_fill
    Symbol.Icon.IconEnum.timer -> R.drawable.ms_timer_fill
    Symbol.Icon.IconEnum.exercise -> R.drawable.ms_exercise_fill
    Symbol.Icon.IconEnum.piano -> R.drawable.ms_piano
    Symbol.Icon.IconEnum.music_note -> R.drawable.ms_music_note
    Symbol.Icon.IconEnum.option -> R.drawable.ms_keyboard_option_key_w700
    Symbol.Icon.IconEnum.graduationcap -> R.drawable.ms_school_fill
    Symbol.Icon.IconEnum.megaphone -> R.drawable.ms_campaign_fill
    Symbol.Icon.IconEnum.rocket -> R.drawable.ms_rocket_launch_fill
    Symbol.Icon.IconEnum.bus -> R.drawable.ms_directions_bus_fill
    Symbol.Icon.IconEnum.bulb -> R.drawable.ms_lightbulb_fill
    Symbol.Icon.IconEnum.bolt -> R.drawable.ms_bolt_fill
    Symbol.Icon.IconEnum.inbox -> R.drawable.ms_inbox_fill
    Symbol.Icon.IconEnum.sun -> R.drawable.ms_wb_sunny_fill
    Symbol.Icon.IconEnum.moon -> R.drawable.ms_bedtime_fill
    Symbol.Icon.IconEnum.question -> R.drawable.ms_question_mark
}
