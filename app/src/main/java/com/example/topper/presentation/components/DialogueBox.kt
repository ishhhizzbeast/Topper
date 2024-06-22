package com.example.topper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.topper.domain.model.Subject

@Composable
fun AddSubjectDialogue(
    isOpen : Boolean,
    title:String = "Add/Update Subject",
    onDismissRequest:()->Unit,
    onConfirmRequest:()->Unit,
    selectedColors:List<Color>,
    subjectName:String,
    goalHours:String,
    onSubjectNameChange:(String)->Unit,
    onGoalHoursChange:(String)->Unit,
    onColorChange:(List<Color>)->Unit
) {
    if (isOpen) {
        var subjectNameError by rememberSaveable {
            mutableStateOf<String?>(null)
        }
        var goalHourError by rememberSaveable {
            mutableStateOf<String?>(null)
        }
        subjectNameError = when{
            subjectName.isBlank()->"please enter subject"
            subjectName.length<2 -> "subject name is too short"
            subjectName.length>20 -> "subject name is too large"
            else-> null
        }
        goalHourError = when{
            goalHours.isBlank()->"please enter goal hours"
            goalHours.toFloatOrNull() == null -> "Invalid Number"
            goalHours.toFloat()<1f -> "please enter goal hour at least 1 hour"
            goalHours.toFloat()>100f -> "please enter realistic goal hours"
            else-> null
        }
        AlertDialog(
            onDismissRequest = {onDismissRequest()},
            title = {
                Text(text = title)
            },
            text = {
                   Column {
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                       ) {
                           Subject.subjectcardcolors.forEach{Colors->
                               Box(
                                   modifier = Modifier
                                       .padding(end = 16.dp)
                                       .size(24.dp)
                                       .clip(CircleShape)
                                       .border(
                                           width = 1.dp,
                                           color = if (Colors == selectedColors) Color.Black
                                           else Color.Transparent,
                                           shape = CircleShape
                                       )
                                       .background(brush = Brush.verticalGradient(Colors))
                                       .clickable { onColorChange(Colors) }
                               )
                           }
                       }
                       OutlinedTextField(value = subjectName, onValueChange = {
                                                                     onSubjectNameChange(it)
                       },
                           label = {
                               Text(text = "Subject Name")
                           },
                           singleLine = true,
                           isError = subjectNameError != null && subjectName.isNotBlank(),
                           supportingText = {
                               Text(text = subjectNameError.orEmpty())
                           }
                       )
                       Spacer(modifier = Modifier.height(10.dp))
                       OutlinedTextField(value = goalHours, onValueChange = {
                                                                     onGoalHoursChange(it)
                       },
                           label = {
                               Text(text = "Goal Study Hours")
                           },
                           singleLine = true,
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                           isError = goalHourError != null && goalHours.isNotBlank(),
                           supportingText = {
                               Text(text = goalHourError.orEmpty())
                           }
                       )
                   }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmRequest() },
                    enabled = (subjectNameError == null && goalHourError == null)) {
                    Text(text = "Save")
                }
            }

        )
    }
}