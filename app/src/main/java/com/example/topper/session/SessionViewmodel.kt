package com.example.topper.session

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topper.domain.model.Session
import com.example.topper.domain.repositroy.SessionRepository
import com.example.topper.domain.repositroy.SubjectRepository
import com.example.topper.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SessionViewmodel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val subjectRepository: SubjectRepository
): ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state = combine(
        _state,
        sessionRepository.getAllSessions(),
        subjectRepository.getAllSubjects()
    ){ state,sessions,subjects->
        state.copy(
            subjects = subjects,
            sessions = sessions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        SessionState()
    )

    fun onEvent(event:SessionEvent){
        when(event){
            SessionEvent.deleteSession -> deleteSession()
            SessionEvent.notifyToupdateSubject -> notifyToupdateSubject()
            is SessionEvent.onDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }
            is SessionEvent.onRelatedToSubjectChange -> {
                _state.update {
                    it.copy(
                        subjectID = event.subject.subjectId,
                        relatedTosubject = event.subject.name
                    )
                }
            }
            is SessionEvent.saveSession -> insertSession(event.duration)
            is SessionEvent.updateSubjectIdAndRelatedToSubject -> {
                _state.update {
                    it.copy(
                        subjectID = event.subjectId,
                        relatedTosubject = event.relatedToSubject
                    )
                }
            }
        }
    }

    private fun notifyToupdateSubject() {
        viewModelScope.launch {
            if (state.value.subjectID == null || state.value.relatedTosubject == null){
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar("Please select the subject first before " +
                            "starting the timer!",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }


    private val _snackBarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackBarEventFlow.asSharedFlow()

    private fun insertSession(duration: Long) {
        viewModelScope.launch {
            val state = _state.value
            if (duration < 36) {
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Session Can not be less than 36 seconds."
                    )
                )
                return@launch
        }
                try {
                    sessionRepository.insertSession(
                        session = Session(
                            sessionSubjectId = state.subjectID ?: -1,
                            relatedToSubject = state.relatedTosubject ?: "",
                            date = Instant.now().toEpochMilli(),
                            duration = duration
                        )
                    )
                    _snackBarEventFlow.emit(
                        SnackbarEvent.showSnackbar("Session Saved Successfully!")

                    )
                } catch (e: Exception) {
                    _snackBarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                            "Session did not saved!:${e.message}"
                        )
                    )
                }
            }
        }

    private fun deleteSession() {
        viewModelScope.launch {
            try{
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackBarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                                "Session Deleted Successfully!")

                    )
                }
            }catch (e:Exception){
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Session Can not be Deleted!",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

}