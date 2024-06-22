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
fun DeleteDialogue(
    isOpen : Boolean,
    title:String,
    onDismissRequest:()->Unit,
    onConfirmRequest:()->Unit,
    bodyText:String
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = title)
            },
            text = {
               Text(text = bodyText)
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmRequest() }){
                    Text(text = "Delete")
                }
            },

        )
    }
}