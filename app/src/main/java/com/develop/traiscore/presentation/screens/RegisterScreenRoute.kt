package com.develop.traiscore.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.viewmodels.LoginViewModel
import com.develop.traiscore.presentation.viewmodels.RegisterViewModel

@Composable
fun RegisterScreenRoute(
    navController: NavController,

) {

    val registerViewModel: RegisterViewModel = viewModel()

    val email by registerViewModel.email.collectAsState()
    val name by registerViewModel.name.collectAsState()
    val lastName by registerViewModel.lastName.collectAsState()
    val birthDate by registerViewModel.birthDate.collectAsState()
    val isTrainer by registerViewModel.isTrainer.collectAsState()
    val isAtleta by registerViewModel.isAtleta.collectAsState()
    val gender by registerViewModel.gender.collectAsState()
    val height by registerViewModel.height.collectAsState()
    val weight by registerViewModel.weight.collectAsState()
    val neck by registerViewModel.neck.collectAsState()
    val chest by registerViewModel.chest.collectAsState()
    val arms by registerViewModel.arms.collectAsState()
    val waist by registerViewModel.waist.collectAsState()
    val thigh by registerViewModel.thigh.collectAsState()
    val calf by registerViewModel.calf.collectAsState()

    // Cuando el registro sea exitoso, navegamos a Main
    LaunchedEffect(registerViewModel.registrationSuccess) {
        registerViewModel.registrationSuccess.collect {
            navController.navigate(NavigationRoutes.Main.route) {
                popUpTo(NavigationRoutes.Register.route) { inclusive = true }
            }
        }
    }



    RegisterScreen(
        email = email,
        onEmailChange = { registerViewModel.email.value = it },
        name = name,
        onNameChange = { registerViewModel.name.value = it },
        lastName = lastName,
        onLastNameChange = { registerViewModel.lastName.value = it },
        birthDate = birthDate,
        onBirthDateChange = { registerViewModel.birthDate.value = it },
        onPhotoClick = { /* TODO: abrir selector de imagen */ },
        isTrainer = isTrainer,
        isAtleta = isAtleta,
        gender = gender,
        onGenderSelect = registerViewModel::onGenderSelect,
        height = height,
        onHeightChange = { registerViewModel.height.value = it },
        weight = weight,
        onWeightChange = { registerViewModel.weight.value = it },
        neck = neck,
        onNeckChange = { registerViewModel.neck.value = it },
        chest = chest,
        onChestChange = { registerViewModel.chest.value = it },
        arms = arms,
        onArmsChange = { registerViewModel.arms.value = it },
        waist = waist,
        onWaistChange = { registerViewModel.waist.value = it },
        thigh = thigh,
        onThighChange = { registerViewModel.thigh.value = it },
        calf = calf,
        onCalfChange = { registerViewModel.calf.value = it },
        onTrainerClick = registerViewModel::onRoleTrainer,
        onAtletaClick = registerViewModel::onRoleAtleta,
        onRegisterClick = registerViewModel::register
    )

    // Observa aquí un evento de "completar registro" (por ejemplo, LiveData o StateFlow)
    // y cuando ocurra, llama a onComplete()
}