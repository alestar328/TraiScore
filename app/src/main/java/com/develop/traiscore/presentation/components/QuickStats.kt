package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R

@Composable
fun QuickStats(
    totalExercises: Int,
    totalSeries: Int,
    totalReps: Int,
    totalWeight: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = stringResource(id = R.string.quickstats_exer),
            value = totalExercises.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = stringResource(id = R.string.quickstats_series),
            value = totalSeries.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = stringResource(id = R.string.quickstats_reps),
            value = totalReps.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = stringResource(id = R.string.quickstats_sumatory),
            value = "${totalWeight.toInt()} kg",
            modifier = Modifier.weight(1f)
        )
    }
}