// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.supabase") {
                useTarget("io.github.jan-tennert.supabase:${requested.name}:${requested.version}")
                because("Redirect old io.supabase group to jan-tennert supabase-kt")
            }
        }
    }
}