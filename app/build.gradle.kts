plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    //Room
    id("com.google.devtools.ksp")
    //Hilt
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    // Google Services & Firebase
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)

}

android {
    namespace = "com.develop.traiscore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.develop.traiscore"
        minSdk = 31
        targetSdk = 35
        versionCode = 26
        versionName = "1.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("C:/Users/newge/AndroidStudioProjects/TraiScore2/key.jks")
            storePassword = "123456"
            keyAlias = "key"
            keyPassword = "123456"
            storeType = "PKCS12"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions += "version"
    productFlavors {
        create("production") {
            dimension = "version"
            applicationId = "com.develop.traiscore"
            applicationIdSuffix = ""
        }
        create("athlete") {
            dimension = "version"
            //applicationId = "com.develop.traiscore.athlete"
            applicationId = "com.develop.traiscore"
            versionNameSuffix = "-athlete"
        }
        create("trainer") {
            dimension = "version"
            applicationId = "com.develop.traiscore.trainer"
            versionNameSuffix = "-trainer"
        }
        create("lite") {
            dimension = "version"
            applicationId = "com.develop.traiscore"
            versionNameSuffix = "-lite"
            minSdk = 24 // Compatible con Android 7+
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
    // ---------------------------------------------------------------------------------------------
    // ANDROIDX CORE
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.collection:collection-ktx:1.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // ---------------------------------------------------------------------------------------------
    // COMPOSE UI
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    testImplementation(composeBom)

    implementation("androidx.compose.foundation:foundation:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material:1.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("androidx.compose.ui:ui-tooling:1.8.2")

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ---------------------------------------------------------------------------------------------
    // NAVIGATION + LIFECYCLE
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.2")

    // ---------------------------------------------------------------------------------------------
    // ROOM
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common)
    ksp(libs.androidx.room.compiler)

    // ---------------------------------------------------------------------------------------------
    // HILT
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ---------------------------------------------------------------------------------------------
    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-crashlytics")

    // ---------------------------------------------------------------------------------------------
    // GOOGLE AUTH & IDENTITY
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation(libs.androidx.credentials.play.services.auth)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // ---------------------------------------------------------------------------------------------
    // CAMERA + MLKIT
    val cameraxVersion = "1.5.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("androidx.camera:camera-mlkit-vision:$cameraxVersion")
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // ---------------------------------------------------------------------------------------------
    // IMAGE & NETWORK
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    // ---------------------------------------------------------------------------------------------
    // DATASTORE
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // ---------------------------------------------------------------------------------------------
    // BILLING / PAYMENTS
    implementation("com.google.android.gms:play-services-wallet:19.4.0")
    implementation("com.google.pay.button:compose-pay-button:1.1.0")
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation(libs.google.pay.api)
    implementation(libs.tasks.api.coroutines)

    // ---------------------------------------------------------------------------------------------
    // UTILS
    implementation("com.google.guava:guava:33.4.8-android")
    implementation(libs.gson)

    // ---------------------------------------------------------------------------------------------
    // IA / GEMINI (solo para non-Lite flavors)
    listOf("athlete", "trainer", "production").forEach { flavor ->
        add("${flavor}Implementation", "com.google.firebase:firebase-ai")
        add("${flavor}Implementation", "com.google.ai.edge.aicore:aicore:0.0.1-exp01")
        add("${flavor}Implementation", "com.google.ai.client.generativeai:generativeai:0.9.0")
    }

    // ---------------------------------------------------------------------------------------------
    // TESTS
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}