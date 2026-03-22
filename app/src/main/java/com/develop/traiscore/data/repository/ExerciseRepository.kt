package com.develop.traiscore.data.repository

import android.util.Log
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // ✅ Flow principal - UI observa esto (como WorkoutRepository)
    val exercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercisesFlow()

    private var userExercisesImported = false
    private var globalExercisesImported = false


    // ✅ Obtener ejercicios una sola vez (para operaciones puntuales)
    suspend fun getExercises(): List<ExerciseEntity> {
        return exerciseDao.getAllExercises()
    }

    /**
     * ✅ Importar ejercicios globales de Firebase (solo primera vez)
     * Se ejecuta al instalar la app o al login
     */
    suspend fun importGlobalExercisesIfNeeded() {
        // 👉 Primera protección: bandera en memoria (evita doble llamada simultánea)
        if (globalExercisesImported) {
            return
        }

        // 👉 Marcar inmediatamente para evitar que otra llamada paralela vuelva a importar
        globalExercisesImported = true

        // 👉 Segunda protección: verificar si Room ya tiene globales guardados
        val count = exerciseDao.getGlobalExerciseCount()
        if (count > 0) {
            return
        }

        try {

            val snapshot = firestore.collection("exercises").get().await()

            val exercises = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val category = doc.getString("category") ?: return@mapNotNull null

                ExerciseEntity(
                    idIntern = doc.id,
                    name = name,
                    category = category,
                    isDefault = true,
                    isSynced = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            // Filtrar posibles duplicados
            val toInsert = exercises.filter { fbExercise ->
                exerciseDao.getExerciseByFirebaseId(fbExercise.idIntern) == null
            }

            if (toInsert.isNotEmpty()) {
                exerciseDao.insertExercises(toInsert)
            }

        } catch (e: Exception) {
            Log.e("ExerciseRepo", "❌ Error importando ejercicios globales", e)
            // 👇 MUY IMPORTANTE:
            // Si falló, reseteamos la bandera para reintentar luego.
            globalExercisesImported = false
        }
    }
    suspend fun cleanupDuplicates() {
        try {
            val allExercises = exerciseDao.getAllExercises()
            val seen = mutableSetOf<String>() // idIntern
            val toDelete = mutableListOf<ExerciseEntity>()

            allExercises.forEach { exercise ->
                val key = "${exercise.idIntern}_${exercise.name}"
                if (key in seen && exercise.idIntern.isNotEmpty()) {
                    toDelete.add(exercise)
                } else {
                    seen.add(key)
                }
            }

            toDelete.forEach { exerciseDao.deleteExercise(it) }
        } catch (e: Exception) {
            Log.e("ExerciseRepo", "❌ Error limpiando duplicados", e)
        }
    }
    /**
     * ✅ Importar ejercicios personalizados del usuario
     * Se ejecuta al login
     */
    suspend fun importUserExercises() {
        if (userExercisesImported) {
            return
        }

        userExercisesImported = true
        val userId = auth.currentUser?.uid ?: return

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("exercises")
                .get()
                .await()

            val firebaseExercises = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val category = doc.getString("category") ?: return@mapNotNull null

                ExerciseEntity(
                    idIntern = doc.id,
                    name = name,
                    category = category,
                    isDefault = false,
                    createdBy = userId,
                    isSynced = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            // 🆕 FILTRAR: Solo insertar los que NO existen ya
            val exercisesToInsert = mutableListOf<ExerciseEntity>()

            firebaseExercises.forEach { fbExercise ->
                val existing = exerciseDao.getExerciseByFirebaseId(fbExercise.idIntern)
                val existingSameNameCategory =
                    exerciseDao.getAllExercises().any {
                        it.name == fbExercise.name &&
                                it.category == fbExercise.category
                    }

                if (existing == null && !existingSameNameCategory) {
                    exercisesToInsert.add(fbExercise)
                }
            }

            if (exercisesToInsert.isNotEmpty()) {
                exerciseDao.insertExercises(exercisesToInsert)
            }
        } catch (e: Exception) {
            Log.e("ExerciseRepo", "❌ Error importando ejercicios del usuario", e)
        }
    }

    /**
     * ✅ Crear nuevo ejercicio (local-first)
     * Guarda en Room inmediatamente, sincroniza con Firebase en background
     */
    suspend fun addExercise(name: String, category: String): Long {
        val userId = auth.currentUser?.uid ?: return -1

        val exercise = ExerciseEntity(
            name = name,
            category = category,
            isDefault = false,
            createdBy = userId,
            isSynced = false,
            pendingAction = "CREATE",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val localId = exerciseDao.insertExercise(exercise)

        // Intentar sincronizar inmediatamente
        syncPendingExercises()

        return localId
    }

    /**
     * ✅ Actualizar ejercicio existente
     */
    suspend fun updateExercise(exercise: ExerciseEntity) {
        val updated = exercise.copy(
            isSynced = false,
            pendingAction = "UPDATE",
            updatedAt = System.currentTimeMillis()
        )

        exerciseDao.updateExercise(updated)

        // Intentar sincronizar inmediatamente
        syncPendingExercises()
    }

    /**
     * ✅ Eliminar ejercicio
     */
    suspend fun deleteExercise(exercise: ExerciseEntity) {
        if (exercise.isDefault) {
            Log.w("ExerciseRepo", "⚠️ No se pueden eliminar ejercicios globales")
            return
        }

        val marked = exercise.copy(
            isSynced = false,
            pendingAction = "DELETE",
            updatedAt = System.currentTimeMillis()
        )

        exerciseDao.updateExercise(marked)

        // Intentar sincronizar inmediatamente
        syncPendingExercises()
    }

    /**
     * ✅ Sincronizar ejercicios pendientes con Firebase
     * Se ejecuta automáticamente después de cada operación
     * También se puede llamar manualmente cuando se recupera conexión
     */
    suspend fun syncPendingExercises() {
        val userId = auth.currentUser?.uid ?: return
        val unsynced = exerciseDao.getUnsyncedExercises()

        if (unsynced.isEmpty()) {
            return
        }

        unsynced.forEach { exercise ->
            try {
                when (exercise.pendingAction) {
                    "CREATE" -> {
                        val docRef = firestore.collection("users")
                            .document(userId)
                            .collection("exercises")
                            .add(mapOf(
                                "name" to exercise.name,
                                "category" to exercise.category,
                                "isDefault" to false,
                                "createdBy" to userId
                            ))
                            .await()

                        // Actualizar con Firebase ID
                        exerciseDao.updateExercise(
                            exercise.copy(
                                idIntern = docRef.id,
                                isSynced = true,
                                pendingAction = null
                            )
                        )
                    }

                    "UPDATE" -> {
                        if (exercise.idIntern.isNotEmpty()) {
                            firestore.collection("users")
                                .document(userId)
                                .collection("exercises")
                                .document(exercise.idIntern)
                                .update(mapOf(
                                    "name" to exercise.name,
                                    "category" to exercise.category
                                ))
                                .await()

                            exerciseDao.updateExercise(
                                exercise.copy(
                                    isSynced = true,
                                    pendingAction = null
                                )
                            )
                        }
                    }

                    "DELETE" -> {
                        if (exercise.idIntern.isNotEmpty()) {
                            firestore.collection("users")
                                .document(userId)
                                .collection("exercises")
                                .document(exercise.idIntern)
                                .delete()
                                .await()

                            exerciseDao.deleteExercise(exercise)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ExerciseRepo", "❌ Error sincronizando ${exercise.name}", e)
                // No hacer nada - quedará pendiente para próximo intento
            }
        }
    }
}