package com.develop.traiscore.data.firebaseData

import com.develop.traiscore.data.local.dao.MedicalStatsDao
import com.develop.traiscore.data.remote.dtos.LabEntryDto
import com.develop.traiscore.data.remote.dtos.MedicalReportDto
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalStatsFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MedicalStatsDao {

    override suspend fun saveReport(userId: String, report: MedicalReportDto): Result<String> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val reportRef = firestore.collection("users")
                .document(userId)
                .collection("medicalStats")
                .document(report.id)

            val batch = firestore.batch()
            batch.set(reportRef, report)

            val entriesRef = reportRef.collection("entries")
            report.entries.forEach { e ->
                val eRef = entriesRef.document(e.id)
                val entryDoc = mapOf(
                    "id" to e.id,
                    "reportId" to report.id,
                    "createdAt" to report.createdAt,
                    "testKey" to e.testKey,
                    "testLabel" to e.testLabel,
                    "value" to e.value,
                    "unitKey" to e.unitKey,
                    "unitLabel" to e.unitLabel,
                    "valueSI" to e.valueSI
                )
                batch.set(eRef, entryDoc)
            }

            batch.commit()
                .addOnSuccessListener { cont.resume(Result.success(report.id), null) }
                .addOnFailureListener { cont.resume(Result.failure(it), null) }
        }

    override suspend fun getReports(userId: String, limit: Int): Result<List<MedicalReportDto>> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            firestore.collection("users").document(userId)
                .collection("medicalStats")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .addOnSuccessListener { qs ->
                    val list = qs.documents.mapNotNull { it.toObject(MedicalReportDto::class.java) }
                    cont.resume(Result.success(list), null)
                }
                .addOnFailureListener { cont.resume(Result.failure(it), null) }
        }

    override suspend fun getEntriesByTest(
        userId: String,
        testKey: String,
        limit: Int
    ): Result<List<LabEntryDto>> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            firestore.collectionGroup("entries")
                .whereEqualTo("testKey", testKey)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .addOnSuccessListener { qs ->
                    val entries = qs.documents.map { d ->
                        LabEntryDto(
                            id = d.getString("id")!!,
                            testKey = d.getString("testKey"),
                            testLabel = d.getString("testLabel") ?: "",
                            value = d.getDouble("value"),
                            unitKey = d.getString("unitKey"),
                            unitLabel = d.getString("unitLabel"),
                            valueSI = d.getDouble("valueSI")
                        )
                    }
                    cont.resume(Result.success(entries), null)
                }
                .addOnFailureListener { cont.resume(Result.failure(it), null) }
        }

    override suspend fun deleteReport(userId: String, reportId: String): Result<Unit> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val reportRef = firestore.collection("users")
                .document(userId)
                .collection("medicalStats")
                .document(reportId)

            // Primero obtener todas las entradas
            reportRef.collection("entries")
                .get()
                .addOnSuccessListener { entriesSnapshot ->
                    val batch = firestore.batch()

                    // Eliminar cada entrada
                    entriesSnapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }

                    // Eliminar el reporte principal
                    batch.delete(reportRef)

                    // Ejecutar batch
                    batch.commit()
                        .addOnSuccessListener {
                            cont.resume(Result.success(Unit), null)
                        }
                        .addOnFailureListener {
                            cont.resume(Result.failure(it), null)
                        }
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it), null)
                }
        }

    override suspend fun updateReport(userId: String, report: MedicalReportDto): Result<String> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val reportRef = firestore.collection("users")
                .document(userId)
                .collection("medicalStats")
                .document(report.id)

            // Actualizar el reporte principal
            reportRef.set(report)
                .addOnSuccessListener {
                    // Actualizar las entradas
                    val batch = firestore.batch()
                    val entriesRef = reportRef.collection("entries")

                    // Eliminar entradas antiguas y añadir nuevas
                    entriesRef.get()
                        .addOnSuccessListener { oldEntries ->
                            oldEntries.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }

                            // Añadir nuevas entradas
                            report.entries.forEach { e ->
                                val eRef = entriesRef.document(e.id)
                                val entryDoc = mapOf(
                                    "id" to e.id,
                                    "reportId" to report.id,
                                    "createdAt" to report.createdAt,
                                    "testKey" to e.testKey,
                                    "testLabel" to e.testLabel,
                                    "value" to e.value,
                                    "unitKey" to e.unitKey,
                                    "unitLabel" to e.unitLabel,
                                    "valueSI" to e.valueSI
                                )
                                batch.set(eRef, entryDoc)
                            }

                            batch.commit()
                                .addOnSuccessListener {
                                    cont.resume(Result.success(report.id), null)
                                }
                                .addOnFailureListener {
                                    cont.resume(Result.failure(it), null)
                                }
                        }
                        .addOnFailureListener {
                            cont.resume(Result.failure(it), null)
                        }
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it), null)
                }
        }

    override suspend fun getReportById(
        userId: String,
        reportId: String
    ): Result<MedicalReportDto?> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            firestore.collection("users")
                .document(userId)
                .collection("medicalStats")
                .document(reportId)
                .get()
                .addOnSuccessListener { document ->
                    val report = document.toObject(MedicalReportDto::class.java)
                    cont.resume(Result.success(report), null)
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it), null)
                }
        }
}