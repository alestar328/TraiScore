package com.develop.traiscore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.develop.traiscore.core.Converters
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.data.local.entity.WorkoutEntry

@Database(
    entities = [WorkoutEntry::class, ExerciseEntity::class],
    version = 3,
    exportSchema = true // Cambia según tus necesidades

)
@TypeConverters(Converters::class) // Agrega esta línea
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
}