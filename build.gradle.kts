// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("ktlintFormat") {
    description = "Formats Kotlin source files using ktlint."
    group = "formatting"
    subprojects {
        tasks.findByName("ktlintFormat")?.let { dependsOn(it) }
    }
}

tasks.register("format") {
    description = "Formats all source files."
    group = "formatting"
    dependsOn("ktlintFormat")
}