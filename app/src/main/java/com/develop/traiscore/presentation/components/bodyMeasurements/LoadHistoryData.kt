package com.develop.traiscore.presentation.components.bodyMeasurements

import android.util.Log
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.presentation.screens.MeasurementHistoryItem
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*


fun loadHistoryData(
    viewModel: BodyStatsViewModel,
    onComplete: (List<MeasurementHistoryItem>, String?) -> Unit
) {
    viewModel.getBodyStatsHistory { success, data, error ->
        if (success && data != null) {
            val items = data.mapIndexedNotNull { index, firebaseData ->
                try {
                    val measurements = (firebaseData["measurements"] as? Map<String, Any>)?.let { measMap ->
                        UserMeasurements(
                            height = (measMap["Height"] as? String)?.toDoubleOrNull() ?: 0.0,
                            weight = (measMap["Weight"] as? String)?.toDoubleOrNull() ?: 0.0,
                            neck = (measMap["Neck"] as? String)?.toDoubleOrNull() ?: 0.0,
                            chest = (measMap["Chest"] as? String)?.toDoubleOrNull() ?: 0.0,
                            arms = (measMap["Arms"] as? String)?.toDoubleOrNull() ?: 0.0,
                            waist = (measMap["Waist"] as? String)?.toDoubleOrNull() ?: 0.0,
                            thigh = (measMap["Thigh"] as? String)?.toDoubleOrNull() ?: 0.0,
                            calf = (measMap["Calf"] as? String)?.toDoubleOrNull() ?: 0.0,
                            lastUpdated = firebaseData["createdAt"] as? Timestamp
                        )
                    } ?: UserMeasurements()

                    MeasurementHistoryItem(
                        id = firebaseData["documentId"] as? String ?: "item_$index",
                        measurements = measurements,
                        gender = firebaseData["gender"] as? String ?: "Male",
                        createdAt = firebaseData["createdAt"] as? Timestamp ?: Timestamp.now(),
                        isLatest = index == 0
                    )
                } catch (e: Exception) {
                    Log.e("HistoryData", "Error parsing item $index", e)
                    null
                }
            }
            onComplete(items, null)
        } else {
            onComplete(emptyList(), error)
        }
    }
}
fun formatDate(date: Date): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}

fun formatTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}