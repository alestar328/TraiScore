package com.develop.traiscore.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.tsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraiScoreTopBar(
    leftIcon: @Composable () -> Unit = {},
    rightIcon: @Composable () -> Unit = {},
    showLogo: Boolean = true
) {
    TopAppBar(
        title = {
            if (showLogo) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        },
        navigationIcon = { leftIcon() },
        actions = { rightIcon() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.tsColors.primaryBackgroundColor
        )
    )
}