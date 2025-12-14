package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Almacena snapshots de rutinas guardadas en fechas específicas.
 * Cada vez que se presiona "Save", se crea una nueva entrada sin sobrescribir.
 */
@Entity(tableName = "routine_history")
data class RoutineHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val routineLocalId: Int,       // FK a RoutineEntity.id
    val savedDate: String,          // Formato: yyyy-MM-dd (para búsquedas fáciles)
    val savedTimestamp: Long,       // Unix timestamp para ordenar
    val userId: String,             // Para filtrar por usuario
    val routineName: String,        // Nombre de la rutina en ese momento
    val sectionsSnapshot: String    // JSON serializado de las secciones (incluye ejercicios con datos)
)
