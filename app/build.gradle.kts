

plugins {
    // Android application plugin for building APKs
    // Plugin de aplicación Android para construir APKs
    alias(libs.plugins.android.application)
    // Kotlin support for Android projects
    // Soporte de Kotlin para proyectos Android
    alias(libs.plugins.kotlin.android)
    // Enables Jetpack Compose support in Kotlin
    // Habilita soporte para Jetpack Compose en Kotlin
    alias(libs.plugins.kotlin.compose)
}
val mapsApiKey: String = project.findProperty("GOOGLE_MAPS_API_KEY") as? String ?: ""
android {
    // The application package namespace
    // El namespace (paquete) de la aplicación
    namespace = "com.example.hoteru"
    // Compile SDK version (target Android API level 36)
    // Versión SDK para compilar (API 36)
    compileSdk = 36

    defaultConfig {
        // Application ID (unique package name)
        // ID de aplicación (nombre de paquete único)
        applicationId = "com.example.hoteru"
        // Minimum SDK version supported by the app
        // Versión mínima de SDK soportada por la app
        minSdk = 24
        // Target SDK version for the app
        // Versión objetivo del SDK para la app
        targetSdk = 36
        // Internal version code for app updates
        // Código interno de versión para actualizaciones
        versionCode = 1
        // Version name visible to users
        // Nombre de versión visible para los usuarios
        versionName = "1.0"
        // Instrumentation test runner class
        // Clase para ejecutar pruebas instrumentadas
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        applicationId = "com.example.hoteru"

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            // Disable code shrinking and obfuscation for release build
            // Deshabilita la reducción y ofuscación de código en compilación release
            isMinifyEnabled = false
            // Use default Proguard rules plus custom rules in proguard-rules.pro
            // Usa reglas Proguard por defecto más reglas personalizadas en proguard-rules.pro
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Set Java source compatibility to Java 17
        // Configura compatibilidad de código fuente Java 17
        sourceCompatibility = JavaVersion.VERSION_17
        // Set Java target compatibility to Java 17
        // Configura compatibilidad de destino Java 17
        targetCompatibility = JavaVersion.VERSION_17
        // Enable support for desugaring Java libraries on older Android versions
        // Habilita soporte para desugar librerías Java en versiones antiguas de Android
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        // Set Kotlin JVM target to Java 17 bytecode
        // Configura el target JVM de Kotlin a bytecode Java 17
        jvmTarget = "17"
    }

    buildFeatures {
        // Enable generation of BuildConfig class with fields like API keys
        // Habilita la generación de la clase BuildConfig con campos como claves API
        buildConfig = true
        // Enable Jetpack Compose support
        // Habilita soporte para Jetpack Compose
        compose = true
    }

    packaging {
        resources {
            // Exclude MongoDB native image property files from packaging to avoid conflicts
            // Excluye archivos de propiedades de imagen nativa de MongoDB para evitar conflictos
            excludes += "META-INF/native-image/org.mongodb/bson/native-image.properties"
        }
    }
}

dependencies {
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    // Core library desugaring to support newer Java APIs on older Android versions
    // Desugar librerías core para soportar APIs Java nuevas en Android antiguos
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("androidx.navigation:navigation-compose:2.8.0")
    // MongoDB synchronous Java driver for connecting to MongoDB server
    // Driver Java síncrono de MongoDB para conectar con servidor MongoDB
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")

    // Jetpack Compose bindings for Google Maps to integrate maps in Compose UI
    // Bindings de Jetpack Compose para Google Maps, para integrar mapas en UI Compose
    implementation("com.google.maps.android:maps-compose:2.7.2")

    // Google Play Services Maps SDK for Android core maps functionality
    // SDK Google Play Services Maps para funcionalidad básica de mapas en Android
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // AndroidX Core KTX for Kotlin extensions and utilities
    // AndroidX Core KTX para extensiones y utilidades en Kotlin
    implementation(libs.androidx.core.ktx)
    // AndroidX Lifecycle runtime with Kotlin coroutines support
    // AndroidX Lifecycle runtime con soporte para coroutines Kotlin
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Jetpack Compose integration with Android activity lifecycle
    // Integración Jetpack Compose con ciclo de vida de actividades Android
    implementation(libs.androidx.activity.compose)
    // Compose Bill of Materials for consistent Compose library versions
    // BOM (Bill of Materials) de Compose para versiones consistentes de librerías
    implementation(platform(libs.androidx.compose.bom))
    // Compose UI core components
    // Componentes básicos de UI para Compose
    implementation(libs.androidx.ui)
    // Compose UI graphics utilities
    // Utilidades gráficas para Compose UI
    implementation(libs.androidx.ui.graphics)
    // Compose UI tooling support for previews and debugging
    // Soporte de herramientas para previews y debugging en Compose UI
    implementation(libs.androidx.ui.tooling.preview)
    // Material Design 3 components for modern UI
    // Componentes Material Design 3 para UI moderna
    implementation(libs.androidx.material3)

    // Unit testing framework JUnit
    // Framework para pruebas unitarias JUnit
    testImplementation(libs.junit)
    // AndroidX JUnit extensions for instrumentation tests
    // Extensiones AndroidX JUnit para pruebas instrumentadas
    androidTestImplementation(libs.androidx.junit)
    // Android UI testing with Espresso framework
    // Pruebas UI Android con framework Espresso
    androidTestImplementation(libs.androidx.espresso.core)
    // Compose testing dependencies matching the Compose BOM version
    // Dependencias para pruebas Compose acorde al BOM de Compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // Compose UI testing utilities with JUnit4 support
    // Utilidades para pruebas Compose UI con soporte JUnit4
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Compose UI tooling for debugging (available in debug builds only)
    // Herramientas para debugging UI Compose (solo en compilaciones debug)
    debugImplementation(libs.androidx.ui.tooling)
    // Manifest support for Compose UI tests (debug builds only)
    // Soporte de manifiesto para pruebas UI Compose (solo debug)
    debugImplementation(libs.androidx.ui.test.manifest)
}
