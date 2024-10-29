package com.uanitteiru.commands

import com.uanitteiru.services.BundleService
import com.uanitteiru.services.GitService
import com.uanitteiru.services.JarFileServices
import com.uanitteiru.services.JavaAndMavenService
import com.uanitteiru.services.MavenCommandsService
import com.uanitteiru.services.PomFileService
import com.uanitteiru.services.QuestionService
import com.uanitteiru.utils.PrettyLogger
import com.uanitteiru.utils.toPowerShellCommand
import picocli.CommandLine.Command
import java.io.File
import java.nio.file.Paths

@Command(name = "bundle-deployer", mixinStandardHelpOptions = true)
class BundleDeployer : Runnable {
    private val prettyLogger = PrettyLogger()
    private val pomFileName = "pom.xml"
    private val engineProjectPath = "C:\\Users\\Noitu\\Devel\\abaco\\fvg-payment-prints"
    private val engineJavaModulesNames = listOf("liquidation-list", "payment-file")
    private val bundlesProjectPath = "C:\\Users\\Noitu\\Devel\\abaco\\agri-bundles"
    private val bundlePath = "config"
    private val tenant = "master"
    private val engineName = "print-engine"
    private val bundleName = "appspay-proc"
    private val bundleSubFolder = "print-flow"

    override fun run() {
        val questionService = QuestionService(prettyLogger)
        val mavenCommandsService = MavenCommandsService(prettyLogger)

        // 1) Exposes Java and Maven Version
        prettyLogger.printInfoMessage("Abaco CLI Deployer Tools")

        JavaAndMavenService(prettyLogger).evaluateMavenAndJavaVersions()

        // 2) Ask user if maven and java are correct.
        val javaVersionIsCorrect = questionService
            .askPositiveOrNegativeQuestionQuestion("Do you want to continue? [y/n]")

        if (!javaVersionIsCorrect) {
            prettyLogger.printWarnMessage("Exiting the application. Bye!")
            return
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Loaded Projects:")
        prettyLogger.printInfoMessage("")

        prettyLogger.printInfoMessage("Engine project:    $engineProjectPath")
        prettyLogger.printInfoMessage("Bundle project:    $bundlesProjectPath")
        prettyLogger.printInfoMessage("")

        val projectsAreCorrect = questionService
            .askPositiveOrNegativeQuestionQuestion("Are loaded projects correct? [y/n]")

        if (!projectsAreCorrect) {
            printExitMessage()
            return
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Searching engine project pom file...")
        prettyLogger.printInfoMessage("")

        val enginePomFile = Paths.get(engineProjectPath, pomFileName).toFile()

        if (!enginePomFile.exists()) {
            prettyLogger.printErrorAndExitMessages("pom file not found for engine project at $engineProjectPath")
            return
        }

        prettyLogger.printInfoMessage("Found main pom file")
        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Retrieving engine current version...")
        prettyLogger.printInfoMessage("")

        val currentEngineProjectVersion = PomFileService(prettyLogger).scanPomFileForVersion(enginePomFile)

        if (currentEngineProjectVersion == null) {
            prettyLogger.printErrorAndExitMessages("No project version found")
            return
        }

        prettyLogger.printInfoMessage("Project version has been found")
        prettyLogger.printInfoMessage("Found version: $currentEngineProjectVersion")
        prettyLogger.printInfoMessage("")

        val nextVersion = questionService.askNextVersionQuestion()

        if (nextVersion.isNullOrEmpty() || nextVersion.isBlank()) {
            prettyLogger.printErrorAndExitMessages("An invalid version has been provided")
            return
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Version $nextVersion will be set on all pom files")
        prettyLogger.printInfoMessage("")

        val nextVersionIsCorrect = questionService
            .askPositiveOrNegativeQuestionQuestion("Are you sure? [y/n]")

        if (!nextVersionIsCorrect) {
            printExitMessage()
            return
        }

        mavenCommandsService.setNewVersionAndCommit(nextVersion, engineProjectPath)

        prettyLogger.printInfoMessage("")

        mavenCommandsService.buildJars(engineProjectPath)

        val bundleService = BundleService(prettyLogger)

        val targetBundleFolder = Paths.get(bundlesProjectPath, bundlePath, tenant, engineName).toFile()

        val latestBundlePaddedVersion = bundleService.getLatestBundlePaddedSerialNumber(bundleName, targetBundleFolder)
            ?: return

        val newBundleName = "$bundleName-$latestBundlePaddedVersion"

        val newBundleFolders = Paths.get(targetBundleFolder.path, newBundleName, bundleSubFolder).toFile()

        if (!newBundleFolders.mkdirs()) {
            prettyLogger.printErrorAndExitMessages("Cannot create target bundle folders")
            return
        }

        val jarFileService = JarFileServices(prettyLogger)

        val areJarFilesCorrectlyMoved = jarFileService
            .moveJarFilesToBundles(engineJavaModulesNames, engineProjectPath, newBundleFolders)

        if (!areJarFilesCorrectlyMoved) return

        val gitService = GitService(prettyLogger)

        gitService.syncRepositoryAndPrepareReleaseBranch(nextVersion, bundlesProjectPath)

        prettyLogger.printInfoMessage("Bundle $bundleName deployed successfully!!!")
    }

    private fun printExitMessage() {
        prettyLogger.printErrorAndExitMessages("You have chosen to exit the application")
    }
}