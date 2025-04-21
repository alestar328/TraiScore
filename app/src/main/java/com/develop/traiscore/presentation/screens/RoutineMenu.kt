package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.core.DefaultCategoryImageMapper
import com.develop.traiscore.core.ImageProvider.getImageForType
import com.develop.traiscore.data.firebaseData.RoutineTypeItem
import com.develop.traiscore.presentation.theme.traiBlue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineMenu(
    onRoutineClick: (String, String) -> Unit, // acepta docId y type
    onAddClick: () -> Unit
) {
    val routineTypes = remember { mutableStateListOf<RoutineTypeItem>() }
    val context = LocalContext.current
    val imageRes = DefaultCategoryImageMapper.getImageFor(DefaultCategoryExer.CHEST)

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("routines")
            .get()
            .addOnSuccessListener { result ->
                val uniqueTypes = mutableSetOf<Pair<String, String>>() // Pair<type, docId>
                for (document in result) {
                    val docId = document.id
                    val clientName = document.getString("clientName") ?: "Cliente"
                    val sections = document.get("sections") as? List<Map<String, Any>> ?: continue

                    for (section in sections) {
                        val type = section["type"] as? String ?: continue
                        if (uniqueTypes.add(Pair(type, docId))) {
                            routineTypes.add(RoutineTypeItem(type, docId, clientName))
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar rutinas", Toast.LENGTH_SHORT).show()
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Rutinas",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = traiBlue
                )
            )
        },
        containerColor = Color.DarkGray
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    routineTypes.forEach { routine ->
                        RoutineItem(
                            name = routine.type,
                            imageResId = getImageForType(routine.type),
                            onClick = {
                                onRoutineClick(routine.documentId, routine.type)
                            }
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva rutina") },
                    containerColor = traiBlue,
                    contentColor = Color.Black,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp, bottom = 75.dp) // subirlo sobre el nav bar
                )
            }
        }
    }
}

@Composable
fun RoutineItem(name: String, imageResId: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray) // Puedes eliminar o mantener este color según la imagen
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 30.sp, color = Color.Black)
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "Imagen de rutina",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(80.dp)
                .width(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineMenuPreview() {
    RoutineMenu(
        onRoutineClick = { docId, type ->
            println("Clicked routine with ID: $docId and Type: $type")
        },
        onAddClick = { println("Add new routine") }
    )
}