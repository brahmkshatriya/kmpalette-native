@file:Suppress("unused")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.native)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

mavenPublishing {
    coordinates(artifactId = "core")
}

kotlin {
    explicitApi()
    jvmToolchain(jdkVersion = 17)
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "${libs.versions.group.get()}.core"
        compileSdk =
            libs.versions.sdk.compile
                .get()
                .toInt()
        minSdk =
            libs.versions.sdk.min
                .get()
                .toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    js {
        browser()
        binaries.library()
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
        binaries.library()
    }

    desktopNative()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "kmpalette-core"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.androidxPalette)
            api(projects.kmpaletteLoader)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines)
        }

        desktopNativeMain.dependencies {
            implementation(libs.compose.native.material3)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        create("nonWebMain") {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            jvmMain.get().dependsOn(this)
            nativeMain.get().dependsOn(this)
            dependencies {
                implementation(libs.androidx.collection)
            }
        }

        create("skikoMain") {
            dependsOn(commonMain.get())
            jvmMain.get().dependsOn(this)
            webMain.get().dependsOn(this)
            nativeMain.get().dependsOn(this)
        }

        listOf(
            "jvmMain",
            "jsMain",
            "wasmJsMain",
            "iosArm64Main",
            "iosSimulatorArm64Main",
            "linuxX64Main",
            "linuxArm64Main",
            "mingwX64Main",
            "macosX64Main",
            "macosArm64Main",
        ).forEach { sourceSetName ->
            val generatedSkikoSources =
                tasks.register<Sync>("prepare${sourceSetName.replaceFirstChar(Char::uppercaseChar)}SkikoSources") {
                    from("src/skikoTargetMain/kotlin")
                    into(layout.buildDirectory.dir("generated/skikoTargetMain/$sourceSetName"))
                }
            named(sourceSetName) {
                kotlin.srcDir(generatedSkikoSources)
            }
        }
    }
}
