plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test_v2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test_v2"
        minSdk = 24
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
    }

    // Add this block to exclude duplicate files
    packaging {
        resources {
            excludes.addAll(listOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE",
                "META-INF/NOTICE"
            ))
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.databinding.compiler.common)
    implementation(fileTree(mapOf(
        "dir" to "C:\\Users\\grann\\AppData\\Local\\Android\\Sdk\\platforms\\android-34",
        "include" to listOf("*.aar", "*.jar"),
    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
    implementation(libs.mpandroidchart)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}