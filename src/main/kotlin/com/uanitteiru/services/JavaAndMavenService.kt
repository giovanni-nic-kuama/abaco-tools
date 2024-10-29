package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import com.uanitteiru.utils.powershell
import com.uanitteiru.utils.readToString

class JavaAndMavenService(private val prettyLogger: PrettyLogger) {
    fun evaluateMavenAndJavaVersions() {
        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Evaluating Maven and Java on the current machine: ")
        prettyLogger.printInfoMessage("")

        val mavenVersionCommand = ProcessBuilder(powershell, "mvn", "--version").start()

        val commandOutputs = mavenVersionCommand.inputStream.readToString().slice(0..2)

        commandOutputs.forEach {
            prettyLogger.printInfoMessage(it)
        }

        prettyLogger.printInfoMessage("")
    }
}