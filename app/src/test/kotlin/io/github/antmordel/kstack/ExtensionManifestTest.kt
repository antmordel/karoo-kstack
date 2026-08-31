package io.github.antmordel.kstack

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The release publishes `manifest.json`, but Karoo only looks for it if the app manifest says
 * where. Losing this declaration costs nothing at build time and silently strands every installed
 * copy on the version it was sideloaded with, so it is checked here.
 */
class ExtensionManifestTest {

    private val androidManifest = File("src/main/AndroidManifest.xml").readText()
    private val buildScript = File("build.gradle.kts").readText()

    @Test
    fun `the app tells Karoo where to find its release manifest`() {
        assertTrue(
            androidManifest.contains(
                """android:name="io.hammerhead.karooext.MANIFEST_URL""""
            ),
        )
        assertTrue(androidManifest.contains("\${karooManifestUrl}"))
    }

    @Test
    fun `the placeholder the app manifest reads is one the build fills in`() {
        assertTrue(buildScript.contains("""manifestPlaceholders["karooManifestUrl"]"""))
        assertTrue(buildScript.contains("manifest.json"))
    }
}
