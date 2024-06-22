package com.example.topper.data.repository

import com.example.topper.data.local.SessionDao
import com.example.topper.domain.model.Session
import com.example.topper.domain.repositroy.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
):SessionRepository {
    override suspend fun insertSession(session: Session) {
        sessionDao.insertSession(session)
    }

    override suspend fun deleteSession(session: Session) {
        sessionDao.deleteSession(session)
    }

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions()
            .map { sessions->
                sessions.sortedBy {
                    it.date
                }
            }
    }

    override fun getRecentFiveSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().take(5)
            .map { sessions->
                sessions.sortedBy {
                    it.date
                }
            }
    }

    override fun getRecentTenSessionsForSubject(subjectId: Int): Flow<List<Session>> {
        return sessionDao.getRecentSessionsForSubject(subjectId).take(10)
            .map { sessions->
                sessions.sortedBy {
                    it.date
                }
            }
    }

    override fun getTotalSessionsDuration(): Flow<Long> {
        return sessionDao.getTotalSessionsDuration()
    }

    override fun getTotalSessionsDurationBySubject(subjectId: Int): Flow<Long> {
        return sessionDao.getTotalSessionsDurationBySubject(subjectId)
    }
}