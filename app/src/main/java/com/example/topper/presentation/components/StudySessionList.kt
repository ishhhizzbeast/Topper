package com.example.topper.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.topper.R
import com.example.topper.domain.model.Session
import com.example.topper.util.toChangeFromMilliToDateString
import com.example.topper.util.toHours
import kotlin.time.Duration.Companion.seconds

fun LazyListScope.sessionScreenList(
    sectionTitle : String,
    session : List<Session>,
    emptyText: String = "You don't have any recent study session.\n" +
            "Start a study session to begin recording your progress",
    onDeleteClick: (Session) -> Unit
){
    item {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
    if (session.isEmpty()) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 8.dp),
                    painter = painterResource(
                        id = R.drawable.img_lamp
                    ),
                    contentDescription = emptyText,
                )
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    items(session){session->
        SessionCard(session = session,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            onDeleteClick = {
                onDeleteClick(session)
            }
        )
    }
}

@Composable
fun SessionCard(
    modifier: Modifier = Modifier,
    session:Session,
    onDeleteClick:()->Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column{
                Text(
                    text = session.relatedToSubject,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = modifier.height(3.dp))
                Text(
                    text = session.date.toChangeFromMilliToDateString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = "${session.duration.toHours()} hr")
                Spacer(modifier = Modifier.width(3.dp))
                IconButton(onClick = { onDeleteClick() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "delete content"
                    )
                }
            }
        }
    }

}