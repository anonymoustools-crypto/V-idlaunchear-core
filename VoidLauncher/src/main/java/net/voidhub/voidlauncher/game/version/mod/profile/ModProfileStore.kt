/*
 * VøidLaunchear project customization.
 * Copyright (C) 2026 Gilbert Jamindang.
 *
 * Based on Zalith Launcher 2; see the root LICENSE and NOTICE files.
 */
package net.voidhub.voidlauncher.game.version.mod.profile

import net.voidhub.voidlauncher.game.version.mod.RemoteMod
import net.voidhub.voidlauncher.game.version.mod.isEnabled
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Stores enabled-mod snapshots separately for each Minecraft version/instance.
 *
 * Profiles are kept beside the mods directory, never inside it, so the normal
 * mod scanner cannot mistake profile files for mods.
 */
class ModProfileStore(
    modsDirectory: File,
    versionKey: String
) {
    private val profileDirectory = File(
        File(modsDirectory.parentFile ?: modsDirectory, ".void_mod_profiles"),
        versionKey.toSafeFileName()
    )

    fun listProfiles(): List<String> {
        if (!profileDirectory.isDirectory) return emptyList()
        return profileDirectory.listFiles { file ->
            file.isFile && file.extension == "properties"
        }.orEmpty().mapNotNull { file ->
            runCatching {
                Properties().apply {
                    FileInputStream(file).use { input -> load(input) }
                }.getProperty("name")?.takeIf(String::isNotBlank)
            }.getOrNull()
        }.distinct().sortedBy { it.lowercase() }
    }

    fun save(profileName: String, enabledFileNames: Collection<String>): Boolean {
        val normalizedName = profileName.trim()
        if (normalizedName.isEmpty()) return false
        if (!profileDirectory.exists() && !profileDirectory.mkdirs()) return false

        val properties = Properties().apply {
            setProperty("name", normalizedName)
            setProperty(
                "enabled",
                enabledFileNames.map(::normalizedModName).distinct().sorted().joinToString("\n")
            )
        }
        val target = profileFile(normalizedName)
        val temporary = File.createTempFile("profile-", ".tmp", profileDirectory)
        return try {
            FileOutputStream(temporary).use { properties.store(it, "VøidLaunchear mod profile") }
            if (target.exists() && !target.delete()) return false
            temporary.renameTo(target)
        } finally {
            temporary.delete()
        }
    }

    /**
     * Applies a snapshot to mods currently present in this instance.
     * Returns filenames from the profile that are not currently installed.
     */
    fun apply(profileName: String, mods: List<RemoteMod>): List<String>? {
        val file = profileFile(profileName)
        if (!file.isFile) return null

        val properties = runCatching {
            Properties().apply { FileInputStream(file).use { input -> load(input) } }
        }.getOrNull() ?: return null

        val enabled = properties.getProperty("enabled", "")
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()

        val installedNames = mods.map { normalizedModName(it.localMod.file.name) }.toSet()
        mods.forEach { mod ->
            val shouldEnable = normalizedModName(mod.localMod.file.name) in enabled
            if (shouldEnable && !mod.localMod.file.isEnabled()) {
                mod.localMod.enable()
            } else if (!shouldEnable && mod.localMod.file.isEnabled()) {
                mod.localMod.disable()
            }
        }
        return (enabled - installedNames).toList()
    }

    fun delete(profileName: String): Boolean {
        val file = profileFile(profileName)
        return !file.exists() || file.delete()
    }

    private fun profileFile(name: String): File =
        File(profileDirectory, "${name.toSafeFileName()}.properties")

    private fun String.toSafeFileName(): String =
        lowercase().map { character ->
            if (character.isLetterOrDigit() || character == '-' || character == '_') character else '-'
        }.joinToString("").trim('-').take(64).ifBlank { "profile" }

    private fun normalizedModName(name: String): String =
        name.removeSuffix(".disabled").removeSuffix(".DISABLED")
}
