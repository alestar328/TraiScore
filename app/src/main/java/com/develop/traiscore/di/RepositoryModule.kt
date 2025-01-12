package com.develop.traiscore.di

import com.develop.traiscore.data.repository.LocalStorageRepositoryImpl
import com.develop.traiscore.domain.repository.LocalStorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLocalStorageRepository(
        localStorageRepository: LocalStorageRepositoryImpl
    ): LocalStorageRepository
}