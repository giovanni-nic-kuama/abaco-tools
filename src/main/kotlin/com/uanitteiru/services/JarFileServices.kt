package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import java.io.File
import java.nio.file.Paths

class JarFileServices(private val prettyLogger: PrettyLogger) {

    fun moveJarFilesToBundles(engineJavaModulesNames: List<String>, engineProjectPath: String, newBundleFolders: File ) : Boolean {
        engineJavaModulesNames.forEach { moduleName ->
            val moduleTargetFolderFileList = Paths.get("$engineProjectPath/$moduleName", "target")
                .toFile()
                .listFiles()?.toList() ?: emptyList()

            val jarFiles = moduleTargetFolderFileList
                .filter { it.name.endsWith(".jar") }
                .filter { !it.name.contains("original") }
                .toList()

            if (jarFiles.isEmpty() || jarFiles.size > 1) {
                prettyLogger.printErrorAndExitMessages("Didn't find the correct jar for module $moduleName")
                return false
            }

            val jarFile = jarFiles[0]

            prettyLogger.printInfoMessage("Copying ${jarFile.name} to ${newBundleFolders.path}...")
            prettyLogger.printInfoMessage("")

            val targetJarFile = File(newBundleFolders, jarFile.name)

            jarFile.copyTo(targetJarFile, true)
        }

        prettyLogger.printInfoMessage("Moved all jar files to ${newBundleFolders.path}")
        prettyLogger.printInfoMessage("")

        return true
    }
}