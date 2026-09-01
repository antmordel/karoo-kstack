package io.github.antmordel.kstack

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A translation goes stale quietly. Adding a field and updating only the English README leaves the
 * Spanish one describing a version of KStack that no longer exists, and nothing else would say so.
 */
class ReadmeTest {

    private val readmes = listOf(File("../README.md"), File("../README.es.md"))

    private val fieldNames = File("src/main/res/values/strings.xml").readText()
        .let { Regex("""<string name="field_[a-z_]+_stack"[^>]*>([^<]+)</string>""").findAll(it) }
        .map { it.groupValues[1] }
        .toList()

    @Test
    fun `every field is named in every README`() {
        readmes.forEach { readme ->
            val text = readme.readText()
            fieldNames.forEach { field ->
                assertTrue("${readme.name} does not mention $field", text.contains(field))
            }
        }
    }

    @Test
    fun `each README links to the other`() {
        val (english, spanish) = readmes

        assertTrue(english.readText().contains("(README.es.md)"))
        assertTrue(spanish.readText().contains("(README.md)"))
    }
}
