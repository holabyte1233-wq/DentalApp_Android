import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "unab.edu.co.abrahamcaceres.dentalapp_android"
    compileSdk = 36  // Required by androidx.core 1.17, androidx.activity 1.12.3, navigation

    defaultConfig {
        applicationId = "unab.edu.co.abrahamcaceres.dentalapp_android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")

        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        fun cleanProp(name: String): String {
            return (properties.getProperty(name) ?: "")
                .trim()
                .removeSurrounding("\"")
        }

        buildConfigField("String", "SUPABASE_URL", "\"${cleanProp("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${cleanProp("SUPABASE_KEY")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${cleanProp("GEMINI_API_KEY")}\"")


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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.security.crypto)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.core)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.google.generativeai)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}