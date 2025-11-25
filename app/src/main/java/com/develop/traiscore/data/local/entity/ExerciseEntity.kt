package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val idIntern: String = "",  // ✅ Ya existe - Firebase ID
    val name: String,
    val category: String = "",
    val isDefault: Boolean = false,

    // 🆕 NUEVOS CAMPOS - Con valores por defecto para no romper datos existentes
    val createdBy: String? = null,        // userId si es personalizado
    val isSynced: Boolean = true,         // Por defecto true = ya sincronizado
    val pendingAction: String? = null,    // null, "CREATE", "UPDATE", "DELETE"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)