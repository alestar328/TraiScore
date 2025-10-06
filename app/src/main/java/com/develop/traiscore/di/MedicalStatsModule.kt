package com.develop.traiscore.di

import com.develop.traiscore.data.firebaseData.MedicalStatsFirestoreRepository
import com.develop.traiscore.data.local.dao.MedicalStatsDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicalStatsModule {

    @Binds
    @Singleton
    abstract fun bindMedicalStatsDao(
        impl: MedicalStatsFirestoreRepository
    ): MedicalStatsDao
}