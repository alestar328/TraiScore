package com.develop.traiscore.di

import android.content.Context
import com.develop.traiscore.data.preferences.ThemePreferences
import com.develop.traiscore.data.repository.InvitationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // ← AGREGAR ESTA FUNCIÓN
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideInvitationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): InvitationRepository {
        return InvitationRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

}