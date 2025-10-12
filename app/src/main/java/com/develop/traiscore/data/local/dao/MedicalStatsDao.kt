package com.develop.traiscore.data.local.dao

import com.develop.traiscore.data.remote.dtos.LabEntryDto
import com.develop.traiscore.data.remote.dtos.MedicalReportDto

interface MedicalStatsDao {
    suspend fun saveReport(userId: String, report: MedicalReportDto): Result<String>
    suspend fun getReports(userId: String, limit: Int): Result<List<MedicalReportDto>>
    suspend fun getEntriesByTest(userId: String, testKey: String, limit: Int = 100): Result<List<LabEntryDto>>
    suspend fun deleteReport(userId: String, reportId: String): Result<Unit>
    suspend fun updateReport(userId: String, report: MedicalReportDto): Result<String>
    suspend fun getReportById(userId: String, reportId: String): Result<MedicalReportDto?>
}