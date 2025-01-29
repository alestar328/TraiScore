package com.develop.traiscore.di

import android.content.Context
import androidx.room.Room
import com.develop.traiscore.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext appContext : Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, "trai_score_database"
        )
            .fallbackToDestructiveMigration() // Permite eliminar y recrear la base de datos
            .build()
    }

    @Singleton
    @Provides
    fun provideWorkoutDao(db: AppDatabase) = db.workoutDao()

    @Singleton
    @Provides
    fun provideExerciseDao(db: AppDatabase) = db.exerciseDao() // Proveer ExerciseDao

}