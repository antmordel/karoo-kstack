package io.github.antmordel.kstack.field

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A definition missing from `extension_info.xml` never reaches the field picker, and an XML entry
 * with no definition is a type Karoo will ask for and never get. Neither fails loudly at runtime,
 * so they are compared here.
 */
class DefinitionsTest {

    private val declaredTypeIds: List<String> by lazy {
        val xml = File("src/main/res/xml/extension_info.xml").readText()
        Regex("""typeId="([^"]+)"""").findAll(xml).map { it.groupValues[1] }.toList()
    }

    @Test
    fun `every definition is declared in extension_info`() {
        assertEquals(Definitions.all.map { it.fieldId }.sorted(), declaredTypeIds.sorted())
    }

    @Test
    fun `every definition names itself with the string the field picker shows`() {
        // The settings screen labels its sections from nameRes while the picker labels the field
        // from the XML. If they drift, the same field has two names on one device.
        val xml = File("src/main/res/xml/extension_info.xml").readText()
        val declaredNames = Regex("""typeId="([^"]+)"\s+displayName="@string/([^"]+)"""")
            .findAll(xml)
            .associate { it.groupValues[1] to it.groupValues[2] }

        Definitions.all.forEach { definition ->
            val resourceName = declaredNames.getValue(definition.fieldId)
            val declared = io.github.antmordel.kstack.R.string::class.java
                .getField(resourceName)
                .getInt(null)
            assertEquals(definition.fieldId, declared, definition.nameRes)
        }
    }

    @Test
    fun `field ids are unique`() {
        val ids = Definitions.all.map { it.fieldId }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every value carries a preview so the page editor is never blank`() {
        Definitions.all.forEach { definition ->
            val values = listOf(definition.primary) + definition.secondaries
            values.forEach { value ->
                assertTrue("${definition.fieldId} has a zero preview", value.previewValue > 0.0)
            }
        }
    }

    @Test
    fun `every secondary is labeled and the primary is not`() {
        Definitions.all.forEach { definition ->
            assertEquals("${definition.fieldId} primary", null, definition.primary.labelRes)
            definition.secondaries.forEach {
                assertTrue("${definition.fieldId} secondary", it.labelRes != null)
            }
        }
    }
}
