plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
}

android {
    namespace = "com.timesheet.app"
    compileSdk = 33

    buildFeatures{
        compose = true
    }

    defaultConfig {
        applicationId = "com.timesheet.app"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    val compose_version = "1.4.3"
    val material_version = "1.4.0"

    implementation("androidx.compose.animation:animation-core:$compose_version")
    implementation("androidx.compose.animation:animation:$compose_version")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.foundation:foundation:$compose_version")
    implementation("androidx.compose.ui:ui-geometry:$compose_version")
    implementation("androidx.compose.ui:ui-graphics:$compose_version")
    implementation("androidx.compose.foundation:foundation-layout:$compose_version")
    implementation("androidx.compose.runtime:runtime-livedata:$compose_version")
    implementation("androidx.compose.material:material:$material_version")
    implementation("androidx.compose.material:material-icons-core:$material_version")
    implementation("androidx.compose.material:material-icons-extended:$material_version")
    implementation("androidx.compose.runtime:runtime-rxjava2:$compose_version")
    implementation("androidx.compose.ui:ui-text:$compose_version")
    implementation("androidx.compose.ui:ui-util:$compose_version")
    implementation("androidx.compose.ui:ui-viewbinding:$compose_version")
    implementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation ("androidx.compose.material3:material3:1.1.1")
    implementation ("androidx.compose.material3:material3-window-size-class:1.1.1")

    wearApp(project(":wear"))
}