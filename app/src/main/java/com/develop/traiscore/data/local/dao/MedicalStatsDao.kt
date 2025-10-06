package com.develop.traiscore.data.local.dao

import com.develop.traiscore.data.remote.dtos.LabEntryDto
import com.develop.traiscore.data.remote.dtos.MedicalReportDto

interface MedicalStatsDao {
    suspend fun saveReport(userId: String, report: MedicalReportDto): Result<String>
    suspend fun getReports(userId: String, limit: Int = 50): Result<List<MedicalReportDto>>
    suspend fun getEntriesByTest(userId: String, testKey: String, limit: Int = 100): Result<List<LabEntryDto>>
}