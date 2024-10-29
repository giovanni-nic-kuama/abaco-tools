package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import com.uanitteiru.utils.powershell
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MavenCommandsService(private val prettyLogger: PrettyLogger) {

    fun setNewVersionAndCommit(version: String, engineProjectPath: String) {
        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Setting version $version on all project modules...")

        ProcessBuilder(powershell, "mvn", "versions:set", "-DnewVersion=''$version''")
            .directory(File(engineProjectPath))
            .start()
            .waitFor()

        prettyLogger.printInfoMessage("Committing the new version on all project modules...")

        ProcessBuilder(powershell, "mvn", "versions:commit")
            .directory(File(engineProjectPath))
            .start()
            .waitFor()
    }

    fun buildJars(engineProjectPath: String) {
        prettyLogger.printInfoMessage("Building jars for all engine modules...")

        val process = ProcessBuilder(powershell, "mvn", "clean", "package", "-DskipTests")
            .directory(File(engineProjectPath))
            .start()
        
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))

        prettyLogger.printInfoMessage("")

        // Read the output from the command
        var outputLockEnabled = true
        var outputText: String? = null
        while ((stdInput.readLine().also { outputText = it }) != null) {
            if (outputText?.contains("Reactor Summary for") == true) outputLockEnabled = false

            if (!outputLockEnabled) {
                prettyLogger.printInfoMessage(outputText!!.replace("[INFO] ", ""))
            }
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Jars built successfully")
        prettyLogger.printInfoMessage("")
    }
}