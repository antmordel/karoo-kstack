plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.antmordel.kstack"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.antmordel.kstack"
        // Karoo 2 is the floor; Karoo 3 is newer. Do not raise without checking Karoo 2.
        minSdk = 26
        targetSdk = 35
        versionCode = System.getenv("RELEASE_VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("RELEASE_VERSION") ?: "0.1.0"
    }

    // The release workflow decodes the keystore secret to a file and points KEYSTORE_FILE at it.
    // Without those variables the release build is unsigned, so a local assembleRelease still works.
    val keystoreFile = System.getenv("KEYSTORE_FILE")?.let(::file)?.takeIf { it.exists() }

    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
        // These two fail whenever Google publishes a release, with no change on our side —
        // a green build would turn red on its own. Dependency currency is Dependabot's job.
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
    }
}

// The Karoo extension index reads manifest.json from the release assets to discover the
// extension and offer the APK. It is generated rather than committed so the version it
// advertises cannot drift from the APK published alongside it.
tasks.register("generateManifest") {
    description = "Writes manifest.json describing the release for the Karoo extension index"
    group = "build"

    val output = layout.buildDirectory.file("manifest.json")
    val versionName = android.defaultConfig.versionName
    val versionCode = android.defaultConfig.versionCode
    // Without declared inputs the task is UP-TO-DATE on the second run and keeps advertising
    // the previous release's version.
    inputs.property("versionName", versionName)
    inputs.property("versionCode", versionCode)
    outputs.file(output)

    doLast {
        val baseUrl = "https://github.com/antmordel/karoo-kstack/releases/latest/download"
        val manifest = mapOf(
            "label" to "KStack",
            "packageName" to "io.github.antmordel.kstack",
            "iconUrl" to "$baseUrl/kstack.png",
            "latestApkUrl" to "$baseUrl/app-release.apk",
            "latestVersion" to versionName,
            "latestVersionCode" to versionCode,
            "developer" to "github.com/antmordel",
            "description" to "Stacked data fields: a large current value with smaller labelled " +
                "values beneath it, for heart rate, heart rate percent, speed, power, cadence " +
                "and lap time, with optional zone colouring.",
            "tags" to listOf("data-fields"),
        )
        output.get().asFile.writeText(groovy.json.JsonBuilder(manifest).toPrettyString())
    }
}


dependencies {
    implementation(libs.hammerhead.karoo.ext)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
