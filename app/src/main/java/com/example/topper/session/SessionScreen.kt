package com.example.topper.session

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.navDeepLink
import com.example.topper.presentation.components.ButtonSheetTask
import com.example.topper.presentation.components.DeleteDialogue
import com.example.topper.presentation.components.sessionScreenList
import com.example.topper.sessions
import com.example.topper.subject
import com.example.topper.ui.theme.Red
import com.example.topper.util.Constants.ACTION_SERVICE_CANCEL
import com.example.topper.util.Constants.ACTION_SERVICE_START
import com.example.topper.util.Constants.ACTION_SERVICE_STOP
import com.example.topper.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit


@Destination(
    deepLinks = [DeepLink(Intent.ACTION_VIEW, uriPattern = "topper://dashboard/session")]
)
@Composable
fun SesstionScreenRoute(
    navigator: DestinationsNavigator,
    timerService: SessionTimerService
) {
    val sessionViewmodel : SessionViewmodel = hiltViewModel()
    val state = sessionViewmodel.state.collectAsStateWithLifecycle().value
    SessionScreen(
        snackbarEvent = sessionViewmodel.snackbarEventFlow,
        state = state,
        sessionViewmodel::onEvent,
        onButtonSheetClick = {
        navigator.navigateUp()
    },
        timerService = timerService)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreen(
    snackbarEvent:SharedFlow<SnackbarEvent>,
    state : SessionState,
    onEvent:(SessionEvent)->Unit,
    onButtonSheetClick: () -> Unit,
    timerService: SessionTimerService
) {
    val hours by timerService.hours
    val minutes by timerService.minutes
    val seconds by timerService.seconds
    val currentTimerState by timerService.currentTimerState
    val context = LocalContext.current
    var isDeleteSessionOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isButtonSheetIsOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val buttomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
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

                SnackbarEvent.NaviagateUp -> {
                    //no need to handle cause after saving the session we do not navigate back.
                }
            }
        }

    }
    LaunchedEffect(key1 = state.subjects) {
        val subjectId = timerService.subjectID.value
        onEvent(
            SessionEvent.updateSubjectIdAndRelatedToSubject(
                subjectId = subjectId,
                relatedToSubject = state.subjects.find {
                    it.subjectId == subjectId }?.name
            )
        )
    }

    Scaffold (
        snackbarHost = {
                       SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            SessionScreenTopBar(onNavigationIconClicked = {
                onButtonSheetClick()
            })
        }
    ){PaddingValues->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues)
        ) {
            item {
                TimerSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds
                )
            }
            item { 
                RelatedToSubjectSession(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    relatedToSubject = state.relatedTosubject ?: "",
                    onButtonSheetClick = {
                        isButtonSheetIsOpen = true
                    },
                    seconds = seconds
                )
            }
            item {
                ButtonSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                    ,
                    onCancelClick = {
                                    ServiceHelper.triggerForegroundServices(
                                        context = context,
                                        action = ACTION_SERVICE_CANCEL
                                    )
                    },
                    onStartClick = {
                        if (state.subjectID != null || state.relatedTosubject != null) {
                            ServiceHelper.triggerForegroundServices(
                                context = context,
                                action = if (currentTimerState == TimerState.STARTED) {
                                    ACTION_SERVICE_STOP
                                } else ACTION_SERVICE_START
                            )
                            timerService.subjectID.value = state.subjectID
                        }else{
                            onEvent(SessionEvent.notifyToupdateSubject)
                        }
                    },
                    onFinishClick = {
                        val duration = timerService.duration.toLong(DurationUnit.SECONDS)
                        if (duration >= 36) {
                            ServiceHelper.triggerForegroundServices(
                                context = context,
                                action = ACTION_SERVICE_CANCEL
                            )
                        }
                        onEvent(SessionEvent.saveSession(duration))
                    },
                    timerState =currentTimerState,
                    seconds =seconds
                )
            }
            sessionScreenList("STUDY SESSION HISTORY", state.sessions, onDeleteClick = {
                onEvent(SessionEvent.onDeleteSessionButtonClick(it))
                isDeleteSessionOpen = true
            })
            item {
                DeleteDialogue(
                    isOpen = isDeleteSessionOpen ,
                    title = "Delete Session",
                    onDismissRequest = {
                        isDeleteSessionOpen = false
                    },
                    onConfirmRequest = {
                        onEvent(SessionEvent.deleteSession)
                        isDeleteSessionOpen = false
                    },
                    bodyText = "Do you sure want to delete this session? your study session will be reduced" +
                            " once you delete this section can not be undone"
                )
            }
            item {
                ButtonSheetTask(
                    isOpen = isButtonSheetIsOpen,
                    SheetState = buttomSheetState,
                    subjects = state.subjects,
                    onDismissCLick = { isButtonSheetIsOpen = false},
                    onSubjectClick ={
                        onEvent(SessionEvent.onRelatedToSubjectChange(it))
                        scope.launch {
                            buttomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!buttomSheetState.isVisible) isButtonSheetIsOpen = false
                        }
                    } ,
                    bottomSheetTitle = "Related To Subject"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreenTopBar(
    onNavigationIconClicked:()->Unit
) {
    TopAppBar(
        navigationIcon = {
                         IconButton(onClick = onNavigationIconClicked) {
                             Icon(
                                 imageVector = Icons.Default.ArrowBack,
                                 contentDescription = "back to main Screen"
                             )
                         }
        },
        title = {
            Text(
                text = "Study Session",
                style= MaterialTheme.typography.headlineSmall
                )
        }
        )
}

@Composable
private fun TimerSection(
    modifier: Modifier,
    hours: String,
    minutes: String,
    seconds: String
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row {
                AnimatedContent(
                    targetState = hours,
                    label = hours,
                    transitionSpec = { timerTextAnimation() }
                ) { hours ->
                    Text(
                        text = "$hours:",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = minutes,
                    label = minutes,
                    transitionSpec = { timerTextAnimation() }
                ) { minutes ->
                    Text(
                        text = "$minutes:",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = seconds,
                    label = seconds,
                    transitionSpec = { timerTextAnimation() }
                ) { seconds ->
                    Text(
                        text = seconds,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedToSubjectSession(
    modifier: Modifier,
    relatedToSubject:String,
    onButtonSheetClick:()->Unit,
    seconds:String
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Related to subject",
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = relatedToSubject)
            IconButton(onClick = onButtonSheetClick, enabled = seconds =="00") {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Selecting subject button",
                )
            }
        }
    }
}

@Composable
private fun ButtonSection(
    modifier: Modifier,
    onCancelClick:()->Unit,
    onStartClick:()->Unit,
    onFinishClick:()->Unit,
    timerState: TimerState,
    seconds: String
) {
    Row (modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
        ){
        Button(onClick = onCancelClick,
            enabled = seconds != "00" && timerState != TimerState.STARTED) {
            Text(text = "Cancel")
        }
        Button(onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timerState == TimerState.STARTED) Red
                else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )) {
            Text(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                text = when (timerState) {
                    TimerState.STARTED -> "Stop"
                    TimerState.STOPPED -> "Resume"
                    else -> "Start"
                })
        }
        Button(onClick = onFinishClick,
                enabled = seconds != "00" && timerState != TimerState.STARTED) {
            Text(text = "Finish")
        }
    }
}
private fun timerTextAnimation(duration: Int = 600): ContentTransform {
    return slideInVertically(animationSpec = tween(duration)) { fullHeight -> fullHeight } +
            fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutVertically(animationSpec = tween(duration)) { fullHeight -> -fullHeight } +
            fadeOut(animationSpec = tween(duration))
}