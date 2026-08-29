pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

// local.properties is read by AGP for sdk.dir; Gradle's property system never looks at it.
// Load it here so gpr.user / gpr.key work the way the karoo-ext README implies they do.
val localProperties = java.util.Properties().apply {
    val file = rootDir.resolve("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun credential(propertyName: String, envName: String): String =
    localProperties.getProperty(propertyName)
        ?: providers.gradleProperty(propertyName).orNull
        ?: System.getenv(envName)
        ?: ""

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // karoo-ext is published only to GitHub Packages, which always requires
        // authentication even though the package is public. Supply gpr.user/gpr.key in
        // local.properties, or GITHUB_ACTOR/GITHUB_PACKAGES_TOKEN in the environment.
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = credential("gpr.user", "GITHUB_ACTOR")
                password = credential("gpr.key", "GITHUB_PACKAGES_TOKEN")
            }
        }
    }
}

rootProject.name = "KStack"
include("app")
