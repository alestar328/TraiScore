package com.develop.traiscore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.dao.WorkoutTypeDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutType

@Database(entities = [WorkoutType::class, WorkoutModel::class, ExerciseEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutTypeDao(): WorkoutTypeDao
}