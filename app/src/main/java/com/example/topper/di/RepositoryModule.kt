package com.example.topper.di

import com.example.topper.data.repository.SessionRepositoryImpl
import com.example.topper.data.repository.SubjectRepositoryImpl
import com.example.topper.data.repository.TaskRepositoryImpl
import com.example.topper.domain.repositroy.SessionRepository
import com.example.topper.domain.repositroy.SubjectRepository
import com.example.topper.domain.repositroy.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindsSubjectRepository(impl : SubjectRepositoryImpl):SubjectRepository

    @Singleton
    @Binds
    abstract fun bindsTaskRepository(impl:TaskRepositoryImpl):TaskRepository

    @Singleton
    @Binds
    abstract fun bindsSessionRepository(impl:SessionRepositoryImpl):SessionRepository
}