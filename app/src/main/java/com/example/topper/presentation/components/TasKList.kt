package com.example.topper.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.topper.R
import com.example.topper.domain.model.Task
import com.example.topper.util.Priority
import com.example.topper.util.toChangeFromMilliToDateString

fun LazyListScope.taskList(
    sectionTitle : String,
    task : List<Task>,
    emptyText: String,
    onCheckBoxClick:(Task)->Unit,
    onTaskCardClick:(Int?)->Unit
){
    item {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
    if (task.isEmpty()) {
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
                        id = R.drawable.img_tasks
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
    items(task){Task->
        TaskCard(task = Task, onCheckBoxClick = {
                                                onCheckBoxClick(Task)
        }, onTaskCardClick = {onTaskCardClick(Task.taskId)},
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}

@Composable
fun TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onCheckBoxClick:()->Unit,
    onTaskCardClick:()->Unit
) {
    ElevatedCard(
        modifier = modifier.clickable {onTaskCardClick()}
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskCheckBox(
                borderColor = Priority.fromInt(task.priority).color,
                onCheckBoxClick = onCheckBoxClick,
                isComplete = task.isComplete
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column{
                Text(
                    text = task.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isComplete){
                        TextDecoration.LineThrough
                    }else TextDecoration.None
                )
                Spacer(modifier = modifier.height(4.dp))
                Text(
                    text = task.dueDate.toChangeFromMilliToDateString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

}