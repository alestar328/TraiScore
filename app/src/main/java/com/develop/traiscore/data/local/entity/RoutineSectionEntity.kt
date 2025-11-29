package com.develop.traiscore.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_sections")
data class RoutineSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val routineLocalId: Int,       // FK a RoutineEntity.id
    val type: String               // nombre del grupo muscular o sección
)
