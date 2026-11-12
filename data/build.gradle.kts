import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

kotlin {
    android {
        namespace = "com.zenith.data"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm)
            implementation(libs.sqlite.jdbc)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.core)
        }
    }
}

sqldelight {
    databases {
        create("ZenithDatabase") {
            packageName.set("com.zenith.data.db")
        }
    }
}
