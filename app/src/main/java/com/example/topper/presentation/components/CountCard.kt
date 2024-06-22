package com.example.topper.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CountCard(
    modifier:Modifier,
    headingText : String,
    count:String
) {
    ElevatedCard(modifier = modifier, elevation = CardDefaults.cardElevation(6.dp)) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = headingText, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = count, style = MaterialTheme.typography.bodySmall.copy(fontSize = 30.sp))
        }
    }
}