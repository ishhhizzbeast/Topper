package com.example.topper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task

@Database(
    entities = [Subject::class,Task::class,Session::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ColorListConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun sessionDao() : SessionDao
    abstract fun taskDao(): TaskDao
    abstract fun subjectDao():SubjectDao

}