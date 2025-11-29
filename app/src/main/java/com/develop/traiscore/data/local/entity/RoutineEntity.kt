package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val routineIdFirebase: String?,    // ID del documento en Firebase (para backup)
    val userId: String,                // propietario (cliente o trainer)
    val trainerId: String?,
    val type: String,
    val createdAt: Long,
    val clientName: String,
    val routineName: String
)
