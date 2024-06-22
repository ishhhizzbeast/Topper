package com.example.topper.presentation.Subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topper.destinations.TaskScreenRouteDestination
import com.example.topper.presentation.components.AddSubjectDialogue
import com.example.topper.presentation.components.CountCard
import com.example.topper.presentation.components.DeleteDialogue
import com.example.topper.presentation.components.sessionScreenList
import com.example.topper.presentation.components.taskList
import com.example.topper.presentation.task.TaskscreenNavArgs
import com.example.topper.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest


data class SubjectScreenNavArgs(
    val subjectID:Int
)

@Destination(navArgsDelegate = SubjectScreenNavArgs::class)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
) {
    val subjectViewmodel : SubjectViewmodel = hiltViewModel()
    val state = subjectViewmodel.state.collectAsStateWithLifecycle().value
    SubjectScreen(
        state = state,
        snackbarEventFlow = subjectViewmodel.snackbarEventFlow,
        onEvent = subjectViewmodel::onEvent,
        onBackButtonClick = {
                            navigator.navigateUp()
        },
        onAddTaskButtonClick = {
            val navArg = TaskscreenNavArgs(taskID = null,subjectID = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onTaskCardClick = {
            val navArgs = TaskscreenNavArgs(taskID = it,subjectID = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArgs))
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state:SubjectState,
    snackbarEventFlow : SharedFlow<SnackbarEvent>,
    onEvent:(SubjectEvent)->Unit,
    onBackButtonClick:()->Unit,
    onAddTaskButtonClick:()->Unit,
    onTaskCardClick:(Int?)->Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    val snacbarHostState = remember{
        SnackbarHostState()
    }

    LaunchedEffect(key1 =true) {
    snackbarEventFlow.collectLatest { event->
        when(event){
            is SnackbarEvent.showSnackbar -> {
                snacbarHostState.showSnackbar(
                    message = event.message,
                    duration = event.duration
                )
            }

            SnackbarEvent.NaviagateUp -> {
                onBackButtonClick()
            }
        }
    }
    }
    LaunchedEffect(key1 = state.goalStudyHours,key2 = state.studiedHours) {
        onEvent(SubjectEvent.updateProgress)
    }

    val isFabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex  == 0  }
    }
    var isSubjectDialogueOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isDeleteSessionOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isDeleteSubjectOpen by rememberSaveable {
        mutableStateOf(false)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snacbarHostState)},
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {

            ScreenTopAppBar(
                title = state.subjectName,
                onArrowBackClick = {
                                   onBackButtonClick()
                },
                onDeleteClick = {
                                isDeleteSubjectOpen = true
                },
                onEditClick = {
                              isSubjectDialogueOpen = true
                },
                ScrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                              onAddTaskButtonClick()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add the task"
                        )
                    },
                    text = {
                        Text(text = "Add Task")
                    },
                    expanded = isFabExpanded,
                )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SubjectOverviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                    ,
                    progress = state.progress,
                    goalHours = state.goalStudyHours,
                    studiedHours = state.studiedHours.toString()
                )
            }
            taskList(
                sectionTitle = "UPCOMING TASK",
               emptyText = "You don't have any upcoming task.\n" +
            "click the + button in a subject screen to add new task.",
                task = state.upcomingTask,
                onCheckBoxClick = {task->
                                  onEvent(SubjectEvent.onTaskCompletedChange(task))
                },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            taskList(
                sectionTitle = "COMPLETED TASK",
                emptyText = "You don't have any completed task.\n" +
            "click the check box on completion of task.",
                task = state.completedTask,
                onCheckBoxClick = {task->
                                  onEvent(SubjectEvent.onTaskCompletedChange(task))
                },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            sessionScreenList(
                "RECENT STUDY SESSION",
                state.recentSession,
                onDeleteClick = {
                    onEvent(SubjectEvent.onDeleteSessionButtonClick(it))
                    isDeleteSessionOpen = true
            }
            )
            item {
                AddSubjectDialogue(
                    isOpen = isSubjectDialogueOpen,
                    onDismissRequest = {
                        isSubjectDialogueOpen = false
                    },
                    onConfirmRequest = {
                        onEvent(SubjectEvent.UpdateSubject)
                        isSubjectDialogueOpen = false
                    },
                    selectedColors =state.selectedCardColors,
                    onSubjectNameChange = {
                        onEvent(SubjectEvent.onSubjectNameChange(it))
                    },
                    onGoalHoursChange = {
                       onEvent(SubjectEvent.onGoalHourChange(it))
                    },
                    subjectName = state.subjectName,
                    goalHours = state.goalStudyHours,
                    onColorChange = {
                        onEvent(SubjectEvent.onSubjectCardColorChange(it))
                    }
                )
            }

            item {
                DeleteDialogue(
                    isOpen = isDeleteSessionOpen ,
                    title = "Delete Session",
                    onDismissRequest = {
                        isDeleteSessionOpen = false
                    },
                    onConfirmRequest = {
                        onEvent(SubjectEvent.deleteSession)
                        isDeleteSessionOpen = false
                    },
                    bodyText = "Do you sure want to delete this session? your study session will be reduced."
                )
                DeleteDialogue(
                    isOpen = isDeleteSubjectOpen ,
                    title = "Delete Subject",
                    onDismissRequest = {
                        isDeleteSubjectOpen = false
                    },
                    onConfirmRequest = {
                        onEvent(SubjectEvent.DeleteSubject)
                        isDeleteSubjectOpen = false
                    },
                    bodyText = "Do you sure want to delete this Subject?" +
                            " your can not undo this section once you deleted."
                )

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTopAppBar(
    title: String,
    onArrowBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    ScrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = ScrollBehavior,
        navigationIcon = {
            IconButton(onClick = onArrowBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back to dashboard"
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "delete subject"
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit subject"
                )
            }
        }
    )
}

@Composable
private fun SubjectOverviewSection(
    modifier: Modifier,
    progress :Float,
    goalHours:String,
    studiedHours :String
) {
    val percentageProgress = remember(key1=progress) {
        (progress * 100).toInt().coerceIn(0,100)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountCard(
            modifier = Modifier.size(height = 100.dp, width = 110.dp),
            headingText = "Goal Study Hour",
            count = goalHours
        )
        CountCard(
            modifier = Modifier.size(height = 100.dp, width = 110.dp),
            headingText = "Studied Hours",
            count = studiedHours
        )
        
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
            ){
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = 1f,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = progress,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
            )
            Text(text = "$percentageProgress%")
        }
    }
}

