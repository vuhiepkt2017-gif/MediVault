pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            library("android-gradle-plugin", "com.android.tools.build:gradle:8.2.2")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
            
            // Core & Compose
            library("androidx-core-ktx", "androidx.core:core-ktx:1.12.0")
            library("androidx-lifecycle-runtime-ktx", "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
            library("androidx-activity-compose", "androidx.activity:activity-compose:1.8.2")
            library("androidx-compose-bom", "androidx.compose:compose-bom:2023.10.01")
            library("androidx-compose-ui", "androidx.compose.ui:ui")
            library("androidx-compose-ui-graphics", "androidx.compose.ui:ui-graphics")
            library("androidx-compose-ui-tooling-preview", "androidx.compose.ui:ui-tooling-preview")
            library("androidx-compose-material3", "androidx.compose.material3:material3")
            library("androidx-navigation-compose", "androidx.navigation:navigation-compose:2.7.7")
            
            // Room
            library("androidx-room-runtime", "androidx.room:room-runtime:2.6.1")
            library("androidx-room-ktx", "androidx.room:room-ktx:2.6.1")
            library("androidx-room-compiler", "androidx.room:room-compiler:2.6.1")
            
            // CameraX
            library("androidx-camera-core", "androidx.camera:camera-core:1.3.1")
            library("androidx-camera-camera2", "androidx.camera:camera-2:1.3.1")
            library("androidx-camera-lifecycle", "androidx.camera:camera-lifecycle:1.3.1")
            library("androidx-camera-view", "androidx.camera:camera-view:1.3.1")
            
            // ML Kit Text Recognition
            library("play-services-mlkit-text-recognition", "com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

            // Coil for Image Loading
            library("coil-compose", "io.coil-kt:coil-compose:2.5.0")

            plugin("android-application", "com.android.application").version("8.2.2")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").version("1.9.22")
            plugin("kotlin-kapt", "org.jetbrains.kotlin.kapt").version("1.9.22")
        }
    }
}

rootProject.name = "MediVault"
include(":app")
