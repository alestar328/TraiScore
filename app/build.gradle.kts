plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    //Room
    id("com.google.devtools.ksp")
    //Hilt
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)

}

android {
    namespace = "com.develop.traiscore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.develop.traiscore"
        minSdk = 30
        targetSdk = 35
        versionCode = 15
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    flavorDimensions += "version"
    productFlavors {
        create("production") { // Nuevo flavor para la version de Google Play
            dimension = "version"
            // No hay applicationIdSuffix, por lo que usará el defaultConfig.applicationId
        }
        create("athlete") {
            dimension = "version"
            applicationIdSuffix  = ".athlete"
            versionNameSuffix = "-athlete"
        }
        create("trainer") {
            dimension = "version"
            applicationIdSuffix = ".trainer"
            versionNameSuffix = "-trainer"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true

    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Allow references to generated code
    kapt {
        correctErrorTypes = true
    }

}

dependencies {
    implementation(libs.androidx.scenecore)
    val activity_version = "1.10.1"
    val camerax_version = "1.5.0"
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    testImplementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.foundation:foundation")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test")



    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")


    implementation("com.google.guava:guava:33.4.8-android")

    implementation("androidx.activity:activity:$activity_version")
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    implementation ("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.2")

    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation(libs.gson)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation(libs.androidx.credentials.play.services.auth)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(libs.androidx.paging.common.android)
    implementation(libs.firebase.crashlytics)
    implementation (platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.collection:collection-ktx:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.9")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.8.7")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material:1.8.1")
    implementation(libs.play.services.fido)
    implementation(libs.firebase.storage.ktx)

    val lifecycle_version = "2.8.7"

    val room_version = "2.6.1"
    implementation(libs.androidx.room.runtime)
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    val nav_version = "2.9.4"
    implementation(libs.androidx.material3.window.size)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    //La mierda de abajo no acepta la palabra 'class'
    implementation(libs.androidx.material3.window.size)
    implementation("androidx.compose.material:material:1.9.1")
    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation (libs.ui)
    implementation(libs.androidx.room.common)

    implementation(libs.google.pay.api)
    implementation(libs.tasks.api.coroutines)
    implementation("com.google.android.gms:play-services-wallet:19.4.0")
    implementation("com.google.pay.button:compose-pay-button:1.1.0")
    implementation("com.android.billingclient:billing-ktx:8.0.0")

    implementation ("com.google.mlkit:text-recognition:16.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

