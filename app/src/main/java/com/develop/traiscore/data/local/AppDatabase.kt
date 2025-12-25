package com.develop.traiscore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.develop.traiscore.core.Converters
import com.develop.traiscore.data.local.dao.BodyStatsDao
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.RoutineDao
import com.develop.traiscore.data.local.dao.SessionDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.SessionEntity
import com.develop.traiscore.data.local.entity.RoutineEntity
import com.develop.traiscore.data.local.entity.RoutineSectionEntity
import com.develop.traiscore.data.local.entity.RoutineExerciseEntity
import com.develop.traiscore.data.local.entity.RoutineHistoryEntity
import com.develop.traiscore.data.local.entity.BodyStatsEntity


@Database(
    entities = [
        WorkoutEntry::class,
        ExerciseEntity::class,
        SessionEntity::class,
        RoutineEntity::class,
        RoutineSectionEntity::class,
        RoutineExerciseEntity::class,
        RoutineHistoryEntity::class,
        BodyStatsEntity::class
               ],
    version = 8,
    exportSchema = false // Cambia según tus necesidades

)
@TypeConverters(Converters::class) // Agrega esta línea
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionDao(): SessionDao
    abstract fun routineDao(): RoutineDao
    abstract fun bodyStatsDao(): BodyStatsDao  // ← AGREGAR


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

        // ← AGREGAR NUEVA MIGRACIÓN
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS body_stats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firebaseId TEXT NOT NULL DEFAULT '',
                        userId TEXT NOT NULL,
                        gender TEXT NOT NULL DEFAULT 'Male',
                        height REAL NOT NULL DEFAULT 0.0,
                        weight REAL NOT NULL DEFAULT 0.0,
                        neck REAL NOT NULL DEFAULT 0.0,
                        chest REAL NOT NULL DEFAULT 0.0,
                        arms REAL NOT NULL DEFAULT 0.0,
                        waist REAL NOT NULL DEFAULT 0.0,
                        thigh REAL NOT NULL DEFAULT 0.0,
                        calf REAL NOT NULL DEFAULT 0.0,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        pendingAction TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)

                // Crear índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_body_stats_userId ON body_stats(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_body_stats_createdAt ON body_stats(createdAt)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_body_stats_firebaseId ON body_stats(firebaseId)")
            }
        }
    }
}