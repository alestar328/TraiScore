package com.develop.traiscore.data.repository

import android.util.Log
import com.develop.traiscore.data.local.dao.RoutineDao
import com.develop.traiscore.data.local.entity.*
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun createRoutineLocal(
        userId: String,
        trainerId: String?,
        type: String,
        routineName: String
    ): Int {

        val routine = RoutineEntity(
            id = 0,
            routineIdFirebase = null,
            userId = userId,
            trainerId = trainerId,
            type = type,
            createdAt = System.currentTimeMillis(),
            clientName = routineName,
            routineName = routineName
        )

        return routineDao.insertRoutine(routine).toInt()
    }
    suspend fun persistRoutineInRoom(
        routineLocalId: Int,
        routineDocument: RoutineDocument
    ) {
        // 1. Borrar ejercicios actuales
        val sections = routineDao.getSections(routineLocalId)
        routineDao.deleteExercises(sections.map { it.id })

        // 2. Reinsertar ejercicios desde routineDocument
        routineDocument.sections.forEach { section ->
            val sectionEntity = sections.firstOrNull { it.type == section.type } ?: return@forEach

            val exercises = section.exercises.map {
                RoutineExerciseEntity(
                    sectionId = sectionEntity.id,
                    name = it.name,
                    series = it.series,
                    reps = it.reps,
                    weight = it.weight,
                    rir = it.rir
                )
            }

            if (exercises.isNotEmpty()) {
                routineDao.insertExercises(exercises)
            }
        }
    }

    suspend fun addSection(
        routineLocalId: Int,
        sectionName: String
    ): Int {
        val section = RoutineSectionEntity(
            routineLocalId = routineLocalId,
            type = sectionName
        )
        return routineDao.insertSections(listOf(section))[0].toInt()
    }
    suspend fun syncRoutinesFromFirebase(userId: String) {
        try {
            // 1. Obtener rutinas desde Firebase
            val firebaseRoutines = firestore.collection("users")
                .document(userId)
                .collection("routines")
                .get()
                .await()

            if (firebaseRoutines.isEmpty) {
                Log.d("RoutineRepository", "No hay rutinas en Firebase para sincronizar")
                return
            }

            Log.d("RoutineRepository", "Sincronizando ${firebaseRoutines.size()} rutinas desde Firebase")

            // 2. Para cada rutina de Firebase
            firebaseRoutines.documents.forEach { doc ->
                val firebaseId = doc.id
                val routineName = doc.getString("routineName") ?: "Rutina sin nombre"
                val clientName = doc.getString("clientName") ?: routineName
                val type = doc.getString("type") ?: "CUSTOM"
                val trainerId = doc.getString("trainerId")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                // 3. Verificar si ya existe en Room (por firebaseId)
                val existingRoutines = routineDao.getRoutines(userId)
                val alreadyExists = existingRoutines.any { it.routineIdFirebase == firebaseId }

                if (alreadyExists) {
                    Log.d("RoutineRepository", "Rutina $routineName ya existe en Room, saltando")
                    return@forEach
                }

                // 4. Crear rutina en Room
                val routineEntity = RoutineEntity(
                    id = 0,
                    routineIdFirebase = firebaseId,
                    userId = userId,
                    trainerId = trainerId,
                    type = type,
                    createdAt = createdAt,
                    clientName = clientName,
                    routineName = routineName
                )
                val localRoutineId = routineDao.insertRoutine(routineEntity).toInt()

                // 5. Obtener secciones de Firebase
                @Suppress("UNCHECKED_CAST")
                val sectionsData = doc.get("sections") as? List<Map<String, Any>> ?: emptyList()

                sectionsData.forEach { sectionMap ->
                    val sectionType = sectionMap["type"] as? String ?: "Unknown"

                    // 6. Crear sección en Room
                    val sectionEntity = RoutineSectionEntity(
                        routineLocalId = localRoutineId,
                        type = sectionType
                    )
                    val localSectionId = routineDao.insertSections(listOf(sectionEntity))[0].toInt()

                    // 7. Obtener ejercicios
                    @Suppress("UNCHECKED_CAST")
                    val exercisesData = sectionMap["exercises"] as? List<Map<String, Any>> ?: emptyList()

                    val exerciseEntities = exercisesData.map { exerciseMap ->
                        RoutineExerciseEntity(
                            sectionId = localSectionId,
                            name = exerciseMap["name"] as? String ?: "",
                            series = (exerciseMap["series"] as? Long)?.toInt() ?: 0,
                            reps = exerciseMap["reps"] as? String ?: "",
                            weight = exerciseMap["weight"] as? String ?: "",
                            rir = (exerciseMap["rir"] as? Long)?.toInt() ?: 0
                        )
                    }

                    // 8. Insertar ejercicios en Room
                    if (exerciseEntities.isNotEmpty()) {
                        routineDao.insertExercises(exerciseEntities)
                    }
                }

                Log.d("RoutineRepository", "✅ Rutina '$routineName' sincronizada a Room")
            }

            Log.d("RoutineRepository", "Sincronización completada exitosamente")

        } catch (e: Exception) {
            Log.e("RoutineRepository", "Error sincronizando rutinas desde Firebase", e)
            throw e
        }
    }
    suspend fun addExercises(
        sectionId: Int,
        exercises: List<SimpleExercise>
    ) {
        val entities = exercises.map {
            RoutineExerciseEntity(
                sectionId = sectionId,
                name = it.name,
                series = it.series,
                reps = it.reps,
                weight = it.weight,
                rir = it.rir
            )
        }
        routineDao.insertExercises(entities)
    }

    suspend fun getAllRoutines(userId: String): List<RoutineDocument> {
        val routines = routineDao.getRoutines(userId)
        val result = mutableListOf<RoutineDocument>()

        for (r in routines) {
            val sectionsEntities = routineDao.getSections(r.id)
            val sections = sectionsEntities.map { sec ->
                val exercises = routineDao.getExercises(sec.id).map {
                    SimpleExercise(
                        name = it.name,
                        series = it.series,
                        reps = it.reps,
                        weight = it.weight,
                        rir = it.rir
                    )
                }
                RoutineSection(type = sec.type, exercises = exercises)
            }

            result.add(
                RoutineDocument(
                    userId = r.userId,
                    trainerId = r.trainerId,
                    documentId = r.id.toString(),
                    type = r.type,
                    createdAt = null,
                    clientName = r.clientName,
                    routineName = r.routineName,
                    sections = sections
                )
            )
        }

        return result
    }

    /** BACKUP EN FIREBASE */
    suspend fun backupRoutineToFirebase(
        routineLocalId: Int
    ) {
        val routine = routineDao.getRoutines(auth.uid ?: "").find { it.id == routineLocalId }
            ?: return

        val sectionsEntities = routineDao.getSections(routineLocalId)
        val sections = sectionsEntities.map { sec ->
            RoutineSection(
                type = sec.type,
                exercises = routineDao.getExercises(sec.id).map {
                    SimpleExercise(
                        name = it.name,
                        series = it.series,
                        reps = it.reps,
                        weight = it.weight,
                        rir = it.rir
                    )
                }
            )
        }

        val doc = hashMapOf(
            "clientName" to routine.clientName,
            "routineName" to routine.routineName,
            "type" to routine.type,
            "trainerId" to routine.trainerId,
            "createdAt" to routine.createdAt, // ✅ Usar createdAt original, no System.currentTimeMillis()
            "sections" to sections
        )

        val userId = routine.userId

        // ✅ CORREGIDO: Actualizar si existe, crear si no existe
        if (routine.routineIdFirebase != null) {
            // Ya tiene ID de Firebase → ACTUALIZAR documento existente
            Log.d("RoutineRepository", "Actualizando rutina existente en Firebase: ${routine.routineIdFirebase}")
            firestore.collection("users")
                .document(userId)
                .collection("routines")
                .document(routine.routineIdFirebase!!)
                .set(doc) // ✅ set() reemplaza el documento completo
                .await()
        } else {
            // No tiene ID de Firebase → CREAR nuevo documento
            Log.d("RoutineRepository", "Creando nueva rutina en Firebase")
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("routines")
                .add(doc)
                .await()

            // Actualizar la rutina local con el ID de Firebase
            updateRoutineFirebaseId(routineLocalId, docRef.id)
            Log.d("RoutineRepository", "Rutina creada en Firebase con ID: ${docRef.id}")
        }
    }
    suspend fun getSections(routineLocalId: Int): List<RoutineSectionEntity> {
        return routineDao.getSections(routineLocalId)
    }

    /** Borrar TODOS los ejercicios de una lista de secciones */
    suspend fun deleteExercises(sectionIds: List<Int>) {
        if (sectionIds.isNotEmpty()) {
            routineDao.deleteExercises(sectionIds)
        }
    }

    /** Borrar TODAS las secciones de una rutina */
    suspend fun deleteSections(routineLocalId: Int) {
        routineDao.deleteSections(routineLocalId)
    }

    /** Borrar la rutina completa */
    suspend fun deleteRoutine(localRoutineId: Int) {
        routineDao.deleteRoutine(localRoutineId)
    }
    suspend fun updateRoutineFirebaseId(localId: Int, firebaseId: String) {
        val routines = routineDao.getRoutines(auth.uid ?: "")
        val r = routines.find { it.id == localId } ?: return

        val updated = r.copy(routineIdFirebase = firebaseId)

        routineDao.updateRoutineFirebaseId(localId, firebaseId)

    }
    /**
     * Guarda un snapshot de la rutina actual con la fecha de hoy
     */
    suspend fun saveRoutineSnapshot(routineLocalId: Int) {
        val userId = auth.uid ?: return

        // Obtener la rutina actual
        val routine = routineDao.getRoutines(userId).find { it.id == routineLocalId }
            ?: return

        // Obtener secciones y ejercicios
        val sectionsEntities = routineDao.getSections(routineLocalId)
        val sections = sectionsEntities.map { sec ->
            RoutineSection(
                type = sec.type,
                exercises = routineDao.getExercises(sec.id).map {
                    SimpleExercise(
                        name = it.name,
                        series = it.series,
                        reps = it.reps,
                        weight = it.weight,
                        rir = it.rir
                    )
                }
            )
        }

        // Serializar secciones a JSON
        val gson = Gson()
        val sectionsJson = gson.toJson(sections)

        // Obtener fecha actual en formato yyyy-MM-dd
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        // Crear snapshot
        val snapshot = RoutineHistoryEntity(
            routineLocalId = routineLocalId,
            savedDate = currentDate,
            savedTimestamp = System.currentTimeMillis(),
            userId = userId,
            routineName = routine.routineName,
            sectionsSnapshot = sectionsJson
        )

        // Guardar en base de datos
        routineDao.insertRoutineHistory(snapshot)
    }

    /**
     * Obtiene todas las fechas que tienen rutinas guardadas (para calendario)
     */
    suspend fun getDatesWithRoutines(userId: String): List<String> {
        return routineDao.getDatesWithRoutines(userId)
    }

    /**
     * Obtiene rutinas guardadas en una fecha específica
     */
    suspend fun getRoutinesByDate(userId: String, date: String): List<RoutineHistoryEntity> {
        return routineDao.getRoutinesByDate(userId, date)
    }

    /**
     * Obtiene rutinas de un mes específico
     * @param monthYear formato: "yyyy-MM" (ejemplo: "2025-12")
     */
    suspend fun getRoutinesByMonth(userId: String, monthYear: String): List<RoutineHistoryEntity> {
        return routineDao.getRoutinesByMonth(userId, "$monthYear%")
    }

    /**
     * Obtiene rutinas de un año específico
     * @param year formato: "yyyy" (ejemplo: "2025")
     */
    suspend fun getRoutinesByYear(userId: String, year: String): List<RoutineHistoryEntity> {
        return routineDao.getRoutinesByYear(userId, "$year%")
    }

    /**
     * Deserializa el JSON de secciones almacenado en el snapshot
     */
    fun deserializeSections(sectionsJson: String): List<RoutineSection> {
        return try {
            val gson = Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<RoutineSection>>() {}.type
            gson.fromJson(sectionsJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}