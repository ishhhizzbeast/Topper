package com.example.topper.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topper.R
import com.example.topper.destinations.SesstionScreenRouteDestination
import com.example.topper.destinations.SubjectScreenRouteDestination
import com.example.topper.destinations.TaskScreenRouteDestination
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task
import com.example.topper.presentation.Subject.SubjectScreenNavArgs
import com.example.topper.presentation.components.AddSubjectDialogue
import com.example.topper.presentation.components.CountCard
import com.example.topper.presentation.components.DeleteDialogue
import com.example.topper.presentation.components.IndividiualCard
import com.example.topper.presentation.components.sessionScreenList
import com.example.topper.presentation.components.taskList
import com.example.topper.presentation.task.TaskscreenNavArgs
import com.example.topper.ui.theme.errorContainerDarkHighContrast
import com.example.topper.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RootNavGraph(start = true)
@Destination
@Composable
fun DashBoardScreenRoute(
    navigator:DestinationsNavigator
) {
    val dashBoardViewmodel : DashboardViewmodel = hiltViewModel()
    val state = dashBoardViewmodel.state.collectAsStateWithLifecycle().value
    val task = dashBoardViewmodel.task.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val session = dashBoardViewmodel.session.collectAsStateWithLifecycle(initialValue = emptyList()).value
    DashBoardScreen(
        
        state = state,
        task = task,
        session = session,
        snackbarEvent = dashBoardViewmodel.snackbarEventFlow,
        onEvent = dashBoardViewmodel::onEvent,
        onSubjectCardClick ={subjectID->
                            subjectID?.let {
                                val navArg = SubjectScreenNavArgs(subjectID = subjectID)
                                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
                            }
        } ,
        onTaskCardClick = {taskID->
                          val navArg = TaskscreenNavArgs(taskID = taskID,null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))        },
        onStartStudySessionClick ={
            navigator.navigate(SesstionScreenRouteDestination())
        }
    )
}
@Composable
private fun DashBoardScreen(
    task:List<Task>,
    session:List<Session>,
    snackbarEvent : SharedFlow<SnackbarEvent>,
    onEvent:(DashboardEvent)->Unit,
    state : DashboardState,
    onSubjectCardClick:(Int?)->Unit,
    onTaskCardClick:(Int?)->Unit,
    onStartStudySessionClick:()->Unit
) {
    var isSubjectDialogueOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isDeleteSessionOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest {event->
           when(event){
               is SnackbarEvent.showSnackbar -> {
                   snackbarHostState.showSnackbar(
                       message = event.message,
                       duration = event.duration
                   )
               }

               SnackbarEvent.NaviagateUp -> TODO()
           }
        }

    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = { DashBoardScreenTopBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CountCardScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    subjectCount = state.totalSubjectCount,
                    studiedHours = "${state.totalStudiedHours}",
                    goalHours = state.totalGoalHours.toString()
                )
            }
            item {
                SubjectCard(
                    modifier = Modifier.fillMaxWidth(),
                    subjectList = state.subjects,
                    onAddClick = {isSubjectDialogueOpen = true},
                    onSubjectCardClick = onSubjectCardClick
                )
            }
            item {
                ElevatedButton(
                    onClick = onStartStudySessionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 28.dp),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Text(text = "Start Study Session")
                }
            }
            taskList("UPCOMING TASK", emptyText = "You don't have any upcoming task.\n" +
                    "click the + button in a subject screen to add new task.", task =  task,
                onCheckBoxClick = {
                                  onEvent(DashboardEvent.onTaskIsCompletedChange(it))
                }, onTaskCardClick = onTaskCardClick)
            sessionScreenList("RECENT STUDY SESSION", session, onDeleteClick = {
                onEvent(DashboardEvent.onDeleteSessionButtonClick(it))
                isDeleteSessionOpen = true
            })




            item {
                AddSubjectDialogue(
                    isOpen = isSubjectDialogueOpen,
                    onDismissRequest = {
                        isSubjectDialogueOpen = false
                    },
                    onConfirmRequest = {
                        onEvent(DashboardEvent.saveSubject)
                        isSubjectDialogueOpen = false
                    },
                    selectedColors = state.subjectCardColors,
                    onSubjectNameChange = {
                        onEvent(DashboardEvent.onSubjectNameChange(it))
                    },
                    onGoalHoursChange = {
                                        onEvent(DashboardEvent.onGoalHoursChange(it))
                    },
                    subjectName = state.subjectName,
                    goalHours = state.goalStudyHours,
                    onColorChange = {
                        onEvent(DashboardEvent.onSubjectCardColorChange(it))
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
                        onEvent(DashboardEvent.deleteSession)
                                       isDeleteSessionOpen = false
                    },
                    bodyText = "Do you sure want to delete this session? your study session will be reduced."
                )
            }

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashBoardScreenTopBar() {
    CenterAlignedTopAppBar(title = {
        Text(text = "Topper", style = MaterialTheme.typography.headlineMedium)
    })
}

@Composable
private fun CountCardScreen(
    modifier: Modifier,
    subjectCount: Int,
    studiedHours: String,
    goalHours: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        CountCard(
            modifier = Modifier.size(100.dp),
            headingText = "Subject Count",
            count = "$subjectCount"
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.size(100.dp),
            headingText = "Studied Hours",
            count = studiedHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.size(100.dp),
            headingText = "Goal Hours",
            count = goalHours
        )
    }
}

@Composable
private fun SubjectCard(
    onSubjectCardClick: (Int?) -> Unit,
    modifier: Modifier,
    subjectList: List<Subject>,
    emptyText: String = "You don't have any subjects.\n click the + button to add new subject.",
    onAddClick:()->Unit
    ) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
            IconButton(onClick = { onAddClick()}) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "add subjects")
            }
        }
        if (subjectList.isEmpty()) {
            Image(
                modifier = modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                painter = painterResource(
                    id = R.drawable.img_books
                ),
                contentDescription = emptyText,
            )
            Text(
                modifier = modifier,
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(subjectList) { subject ->
                IndividiualCard(
                    modifier = Modifier,
                    subjectName = subject.name,
                    gradientColors = subject.colors.map {
                                                Color(it)
                    },
                    onClick = {onSubjectCardClick(subject.subjectId)}
                )
            }
        }
    }
}
