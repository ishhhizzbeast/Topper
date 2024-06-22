package com.example.topper.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.topper.ui.theme.Green
import com.example.topper.ui.theme.Orange
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Priority(val title:String, val color: Color, val value: Int ){
    LOW("Low", color = Green,1),
    MEDIUM("Medium",color = Orange,2),
    HIGH("High",color = Color.Red,3);

    companion object{
        fun fromInt(value:Int) = entries.firstOrNull{it.value == value} ?: MEDIUM
    }
}

fun Long?.toChangeFromMilliToDateString():String{
    val date:LocalDate = this?.let {
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } ?: LocalDate.now(ZoneId.systemDefault())
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun Long.toHours():Float{
    val hours = this.toFloat() / 3600f
    return "%.2f".format(hours).toFloat()
}

sealed class SnackbarEvent{
    data class showSnackbar(val message:String,val duration: SnackbarDuration = SnackbarDuration.Short):SnackbarEvent()
    data object NaviagateUp:SnackbarEvent()
}

fun Int.pad():String{
    return this.toString().padStart(length = 2, padChar = '0')
}
