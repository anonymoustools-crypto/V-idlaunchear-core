package net.voidhub.voidlauncher.game.version.mod.profile

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModProfileStoreTest {
    @Test
    fun savesListsAndDeletesProfiles() {
        val root = Files.createTempDirectory("void-mod-profiles").toFile()
        try {
            val modsDirectory = File(root, "mods").apply { mkdirs() }
            val store = ModProfileStore(modsDirectory, "1.20.1")

            assertTrue(store.save("Performance", listOf("sodium.jar", "lithium.jar")))

            assertEquals(listOf("Performance"), store.listProfiles())
            assertTrue(store.delete("Performance"))
            assertTrue(store.listProfiles().isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun profilesAreSeparatedByMinecraftVersion() {
        val root = Files.createTempDirectory("void-mod-profile-versions").toFile()
        try {
            val modsDirectory = File(root, "mods").apply { mkdirs() }
            val firstVersion = ModProfileStore(modsDirectory, "1.20.1")
            val secondVersion = ModProfileStore(modsDirectory, "1.21.1")

            assertTrue(firstVersion.save("Default", listOf("sodium.jar")))
            assertEquals(listOf("Default"), firstVersion.listProfiles())
            assertTrue(secondVersion.listProfiles().isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }
}
