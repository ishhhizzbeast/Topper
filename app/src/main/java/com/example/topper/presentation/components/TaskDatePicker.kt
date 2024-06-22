package com.example.topper.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePicker(
    state:DatePickerState,
    isOpen:Boolean,
    onDissmissClicked:()->Unit,
    onConfirmClicked:()->Unit,

) {
    if (isOpen) {
        DatePickerDialog(
            onDismissRequest = onDissmissClicked,
            confirmButton = {
                TextButton(onClick = {
                    onConfirmClicked()
                }) {
                    Text(text = "Select")
                }
            },
            dismissButton = {
                TextButton(onClick = onDissmissClicked) {
                    Text(text = "Cancel")
                }
            },
            content = {
                DatePicker(
                    state = state,
                    dateValidator = {timeStamp->
                        val selectedDate = Instant
                            .ofEpochMilli(timeStamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val currentDate = LocalDate.now(ZoneId.systemDefault())
                        selectedDate >= currentDate
                    }
                )
            }
        )
    }
}