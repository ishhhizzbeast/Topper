package com.example.topper.session

import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject

sealed class SessionEvent {
    data class onDeleteSessionButtonClick(val session:Session):SessionEvent()
    data object deleteSession : SessionEvent()
    data class onRelatedToSubjectChange(val subject:Subject) : SessionEvent()
    data class saveSession(val duration: Long):SessionEvent()
    data class updateSubjectIdAndRelatedToSubject(
        val subjectId:Int?,
        val relatedToSubject: String?
    ):SessionEvent()
    data object notifyToupdateSubject:SessionEvent()
}