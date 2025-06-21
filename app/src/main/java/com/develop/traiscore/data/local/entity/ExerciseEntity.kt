package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idIntern: String = "",
    val name: String,
    val isDefault: Boolean = false,
    val category: String = "" // ✅ AGREGADO: Campo category que faltaba

)