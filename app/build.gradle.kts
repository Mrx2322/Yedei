plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.deiapp.yedei"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.deiapp.yedei"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }
}

dependencies {

    // Componentes principales de Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Listas de movimientos, categorías e historial
    implementation(libs.androidx.recyclerview)

    // ViewModel y observación de datos
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Base de datos local Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Generador de código de Room
    ksp(libs.androidx.room.compiler)

    // Tareas asíncronas
    implementation(libs.kotlinx.coroutines.android)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}