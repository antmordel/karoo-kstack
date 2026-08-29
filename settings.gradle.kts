pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

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
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR") ?: "")
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_PACKAGES_TOKEN") ?: "")
            }
        }
    }
}

rootProject.name = "KStack"
include("app")
