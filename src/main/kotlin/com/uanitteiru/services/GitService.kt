package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import com.uanitteiru.utils.powershell
import com.uanitteiru.utils.readToString
import java.io.File

class GitService(private val prettyLogger: PrettyLogger) {

    fun syncRepositoryAndPrepareReleaseBranch(version: String, bundlesProjectPath: String) {
        val bundlesProjectDirectory = File(bundlesProjectPath)
        val branchName = "release/fvg-payment-prints"
        val commitMessage = "release fvg-payment-prints version $version"

        prettyLogger.printInfoMessage("Updating local repository...")

        ProcessBuilder(powershell, "git", "fetch", "--all", "--prune", "--prune-tags")
            .directory(bundlesProjectDirectory)
            .start()
            .waitFor()

        ProcessBuilder(powershell, "git", "checkout", "master")
            .directory(bundlesProjectDirectory)
            .start()
            .waitFor()

        prettyLogger.printInfoMessage("Pulling remote changes...")

        ProcessBuilder(powershell, "git", "pull", "origin", "master")
            .directory(bundlesProjectDirectory)
            .start()
            .waitFor()

        prettyLogger.printInfoMessage("Creating new release branch...")

        ProcessBuilder(powershell, "git", "switch", "-c", branchName)
            .directory(bundlesProjectDirectory)
            .start()
            .waitFor()

        ProcessBuilder(powershell, "git", "add", ".")
            .directory(bundlesProjectDirectory)
            .start()
            .waitFor()

        prettyLogger.printInfoMessage("Committing changes...")
        prettyLogger.printInfoMessage("")

        val start = ProcessBuilder(powershell, "git", "commit", "-m", "'$commitMessage'")
            .directory(bundlesProjectDirectory)
            .start()

        start.inputStream.readToString().forEach {
            prettyLogger.printWarnMessage(it)
        }

        prettyLogger.printInfoMessage("")

        prettyLogger.printInfoMessage("Branch is ready to be pushed to remote repository.")
    }

}