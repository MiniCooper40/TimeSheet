plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.app.timesheet"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.app.timesheet"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material:material")

    val activity_version = "1.7.2"
    // Kotlin
    implementation ("androidx.activity:activity-ktx:$activity_version")
    implementation ("androidx.activity:activity-compose:1.7.2")

    val room_version = "2.5.2"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    val nav_version = "2.6.0"

    implementation("androidx.navigation:navigation-compose:$nav_version")


    // Includes the core logic for charts and other elements.
    implementation ("com.patrykandpatrick.vico:core:1.8.0")

    // For Jetpack Compose.
    implementation ("com.patrykandpatrick.vico:compose:1.8.0")

    // For the view system.
    implementation ("com.patrykandpatrick.vico:views:1.8.0")

    // For `compose`. Creates a `ChartStyle` based on an M2 Material Theme.
    implementation ("com.patrykandpatrick.vico:compose-m2:1.8.0")

    // For `compose`. Creates a `ChartStyle` based on an M3 Material Theme.
    implementation ("com.patrykandpatrick.vico:compose-m3:1.8.0")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation ("io.mhssn:colorpicker:1.0.0")

    wearApp(project(":wear"))
}