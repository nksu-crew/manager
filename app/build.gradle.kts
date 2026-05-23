plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version embeddedKotlinVersion
}


fun gitCommitCount(): Int =
    ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .directory(rootDir)
        .start()
        .inputStream.bufferedReader().readText().trim().toInt()

fun gitCommitHash(): String =
    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(rootDir)
        .start()
        .inputStream.bufferedReader().readText().trim()

android {
    namespace = "me.nekosu.aqnya"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "me.nekosu.aqnya"
        minSdk = 27
        targetSdk = 36
        versionCode = gitCommitCount()
        versionName = gitCommitHash()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    signingConfigs {
        create("debugKey") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugKey")
        }
        release {
            val withR8 = (project.findProperty("withR8") as? String)?.toBoolean() ?: true
            signingConfig = signingConfigs.getByName("debugKey")
            isMinifyEnabled = withR8
            isShrinkResources = withR8
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// ============================================================
// ktlint 代码格式化（通过 Maven Central JAR，跨平台）
// ============================================================

val ktlintVersion = "1.8.0"
val ktlint by configurations.creating

val ktlintFormat by tasks.registering(JavaExec::class) {
    group = "ktlint"
    description = "Format Kotlin source files with ktlint"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "app/src/**/*.kt")
    isIgnoreExitValue = true
}

// ============================================================
// libncore.so 存在性检查
// 本地开发时若无 Go 环境，可提前放置预编译的 .so 文件
// ============================================================

val ncoreLibPath = "src/main/jniLibs/arm64-v8a/libncore.so"

val checkNcoreLib by tasks.registering {
    group = "ncore"
    description = "Check if libncore.so exists; skip Go compilation if present"

    doLast {
        val libFile = file(ncoreLibPath)
        if (!libFile.exists()) {
                throw GradleException("""
                |============================================================
                |libncore.so not found!
                |
                |Expected at: ${libFile.absolutePath}
                |
                |To fix (choose one):
                |  1. Download pre-built libncore.so from CI artifacts and place it at:
                |     app/src/main/jniLibs/arm64-v8a/libncore.so
                |
                |  2. Build from source:
                |     git clone https://github.com/FMAC-Team/nekosu.git
                |     cd nekosu/userspace/libncore
                |     CGO_ENABLED=1 GOOS=android GOARCH=arm64 \
                |       CC=<ndk-toolchain>/aarch64-linux-android34-clang \
                |       go build -buildmode=c-shared -o libncore.so .
                    |     cp libncore.so ${libFile.absolutePath}
                |============================================================
            """.trimMargin())
        }
    }
}

tasks.named("preBuild") {
    dependsOn(checkNcoreLib)
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation("com.google.android.play:core:1.10.3")
    implementation(libs.kotlinx.serialization.json)
    implementation("me.nekosu.flutter_nekosu:flutter_release:1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}