package com.develop.traiscore.data.repository

import com.develop.traiscore.data.local.dao.RoutineDao
import com.develop.traiscore.data.local.entity.*
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
            "createdAt" to System.currentTimeMillis(),
            "sections" to sections
        )

        val userId = routine.userId

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("routines")
            .add(doc)
            .await()

        // Actualizamos la rutina local con el ID de Firebase
        // (te doy la función pero se implementa abajo)
        updateRoutineFirebaseId(routineLocalId, docRef.id)
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

        routineDao.deleteRoutine(localId) // Replace pattern simple
        routineDao.insertRoutine(updated)
    }
}