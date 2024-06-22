package com.example.topper.session

import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject

data class SessionState (
    val subjects : List<Subject> = emptyList(),
    val relatedTosubject:String? = null,
    val sessions:List<Session> = emptyList(),
    val subjectID:Int? = null,
    val session:Session? = null
)