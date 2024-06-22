package com.example.topper.domain.repositroy

import com.example.topper.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository{

    suspend fun upsertSubject(subject:Subject)

    fun getTotalSubjectCount(): Flow<Int>

    fun getTotalGoalHours(): Flow<Float>

    suspend fun deleteSubject(subjectId: Int)

    suspend fun getSubjectById(subjectId: Int): Subject?

    fun getAllSubjects(): Flow<List<Subject>>
}