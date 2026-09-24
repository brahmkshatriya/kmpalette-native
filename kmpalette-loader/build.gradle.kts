import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.native)
    alias(libs.plugins.android.library)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

mavenPublishing {
    coordinates(artifactId = "loader")
}

kotlin {
    explicitApi()
    jvmToolchain(jdkVersion = 17)
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "${libs.versions.group.get()}.loader"
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
            baseName = "kmpalette-loader"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.ui)
        }
        desktopNativeMain.dependencies {
            implementation(libs.compose.native.ui)
        }
    }
}
