package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import java.io.File
import java.nio.file.Paths

class JarFileServices(private val prettyLogger: PrettyLogger) {
    fun moveJarFilesToBundles(
        engineJavaModulesName: String,
        engineProjectPath: String,
        newBundleFolders: File
    ): Boolean {
        val moduleTargetFolderFileList = Paths.get("$engineProjectPath/$engineJavaModulesName", "target")
            .toFile()
            .listFiles()?.toList() ?: emptyList()

        val jarFiles = moduleTargetFolderFileList
            .filter { it.name.endsWith(".jar") }
            .filter { !it.name.contains("original") }
            .toList()

        if (jarFiles.isEmpty() || jarFiles.size > 1) {
            prettyLogger.printErrorAndExitMessages("Didn't find the correct jar for module $engineJavaModulesName")
            return false
        }

        val jarFile = jarFiles[0]

        prettyLogger.printInfoMessage("Moving ${jarFile.name} to ${newBundleFolders.path}")

        val targetJarFile = File(newBundleFolders, jarFile.name)

        jarFile.copyTo(targetJarFile, true)

        return true
    }
}