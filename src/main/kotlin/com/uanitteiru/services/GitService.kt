package com.uanitteiru.services

import com.uanitteiru.data.BuildConfiguration
import com.uanitteiru.utils.PrettyLogger
import com.uanitteiru.utils.powershell
import com.uanitteiru.utils.readToString
import java.io.File

class GitService(private val prettyLogger: PrettyLogger) {

    fun syncRepositoryAndPrepareReleaseBranch(version: String, buildConfiguration: BuildConfiguration) {
        val bundlesProjectDirectory = File(buildConfiguration.bundlesProjectPath)
        val branchName = "release/${buildConfiguration.configurationName}"
        val commitMessage = "release ${buildConfiguration.configurationName} version: $version"

        prettyLogger.printInfoMessage("Updating bundle local repository...")

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

        val gitCommitProcess = ProcessBuilder(powershell, "git", "commit", "-m", "'$commitMessage'")
            .directory(bundlesProjectDirectory)
            .start()

        gitCommitProcess.inputStream.readToString().forEach {
            prettyLogger.printWarnMessage(it)
        }

        prettyLogger.printInfoMessage("")

        if (buildConfiguration.autoPushEnabled) {
            ProcessBuilder(powershell, "git", "push", "origin", branchName)
                .directory(bundlesProjectDirectory)
                .start()
                .waitFor()

            prettyLogger.printInfoMessage("Pushing branch to remote repository.")
            prettyLogger.printInfoMessage("")

            return
        }

        prettyLogger.printInfoMessage("Bundle branch is ready to be pushed to remote repository.")
    }

    fun prepareReleaseBranchForEngine(version: String, buildConfiguration: BuildConfiguration) {
        val bundlesProjectDirectory = File(buildConfiguration.engineProjectPath)
        val branchName = "release/${buildConfiguration.configurationName}"
        val commitMessage = "release version: $version"

        prettyLogger.printInfoMessage("Creating new release branch on engine project...")

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

        if (buildConfiguration.autoPushEnabled) {
            ProcessBuilder(powershell, "git", "push", "origin", branchName)
                .directory(bundlesProjectDirectory)
                .start()
                .waitFor()

            prettyLogger.printInfoMessage("Pushing branch to remote repository.")
            prettyLogger.printInfoMessage("")

            return
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Engine branch is ready to be pushed to remote repository.")
        prettyLogger.printInfoMessage("")
    }
}