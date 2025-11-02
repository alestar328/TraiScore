package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val name: String,
    val color: String,
    val userId: String,
    val createdAt: Date,
    val isActive: Boolean = false,
    val isFinished: Boolean = false,
    val endedAt: Date? = null,

    // Campos para sincronización
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val pendingAction: String? = null // "CREATE", "UPDATE", "DELETE"
)
