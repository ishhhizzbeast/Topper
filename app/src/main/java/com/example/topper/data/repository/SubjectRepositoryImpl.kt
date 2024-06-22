package com.example.topper.data.repository

import com.example.topper.data.local.SubjectDao
import com.example.topper.domain.model.Subject
import com.example.topper.domain.repositroy.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val subjectdao : SubjectDao
) : SubjectRepository{
    override suspend fun upsertSubject(subject: Subject) {
        subjectdao.upsertSubject(subject)
    }

    override fun getTotalSubjectCount(): Flow<Int> {
       return subjectdao.getTotalSubjectCount()
    }

    override fun getTotalGoalHours(): Flow<Float> {
        return subjectdao.getTotalGoalHours()
    }

    override suspend fun deleteSubject(subjectId: Int) {
        subjectdao.deleteSubject(subjectId)
    }

    override suspend fun getSubjectById(subjectId: Int): Subject? {
        return subjectdao.getSubjectById(subjectId)
    }

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectdao.getAllSubjects()
    }
}