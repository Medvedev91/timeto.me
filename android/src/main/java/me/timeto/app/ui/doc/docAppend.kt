package me.timeto.app.ui.doc

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import me.timeto.app.ui.c

fun AnnotatedString.Builder.docAppendRedSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.red)) {
        append(text)
    }
}

fun AnnotatedString.Builder.docAppendGreenSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.green)) {
        append(text)
    }
}

fun AnnotatedString.Builder.docAppendBlueSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.blue)) {
        append(text)
    }
}
