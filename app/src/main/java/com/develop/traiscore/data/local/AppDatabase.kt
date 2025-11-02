package com.develop.traiscore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.develop.traiscore.core.Converters
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.SessionDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.SessionEntity

@Database(
    entities = [WorkoutEntry::class, ExerciseEntity::class, SessionEntity::class],
    version = 4,
    exportSchema = false // Cambia según tus necesidades

)
@TypeConverters(Converters::class) // Agrega esta línea
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionDao(): SessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sessions (
                        sessionId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        color TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 0,
                        isFinished INTEGER NOT NULL DEFAULT 0,
                        endedAt INTEGER,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        lastModified INTEGER NOT NULL,
                        pendingAction TEXT
                    )
                """)
            }
        }
    }
}