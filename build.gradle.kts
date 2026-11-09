import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    pluginManager.withPlugin("dev.detekt") {
        extensions.configure<DetektExtension>("detekt") {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom(rootProject.file("config/detekt.yml"))
        }
        tasks.withType<Detekt>().configureEach {
            jvmTarget.set("17")
            exclude("**/*.kts")
            exclude { element ->
                element.file.invariantSeparatorsPath.contains("/build/")
            }
            reports {
                sarif.required.set(false)
                html.required.set(false)
                markdown.required.set(false)
                checkstyle.required.set(false)
            }
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            tasks.named("detekt").configure {
                dependsOn(
                    "detektCommonMainSourceSet",
                    "detektCommonTestSourceSet",
                    "detektAndroidMainSourceSet",
                    "detektIosMainSourceSet",
                    "detektJvmMainSourceSet",
                    "detektJvmTestSourceSet",
                    "detektMainJvm",
                    "detektTestJvm",
                )
            }
        }
    }
    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<KtlintExtension>("ktlint") {
            filter {
                exclude("**/*.kts")
                exclude { element ->
                    element.file.path.replace('\\', '/').contains("/build/")
                }
            }
        }
        tasks.withType<BaseKtLintCheckTask>().configureEach {
            exclude { element ->
                element.file.invariantSeparatorsPath.contains("/build/")
            }
        }
        tasks.matching { it.name.contains("KotlinScript") }.configureEach {
            enabled = false
        }
    }
}
