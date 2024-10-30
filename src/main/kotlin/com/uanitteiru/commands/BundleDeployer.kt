package com.uanitteiru.commands

import com.uanitteiru.services.BundleService
import com.uanitteiru.services.ConfigurationService
import com.uanitteiru.services.GitService
import com.uanitteiru.services.JarFileServices
import com.uanitteiru.services.JavaAndMavenService
import com.uanitteiru.services.MavenCommandsService
import com.uanitteiru.services.PomFileService
import com.uanitteiru.services.QuestionService
import com.uanitteiru.utils.PrettyLogger
import java.nio.file.Paths
import picocli.CommandLine.Command

@Command(name = "bundle-deployer", mixinStandardHelpOptions = true)
class BundleDeployer : Runnable {
    private val prettyLogger = PrettyLogger()
    private val configurationService = ConfigurationService(prettyLogger)
    private val questionService = QuestionService(prettyLogger)
    private val mavenCommandsService = MavenCommandsService(prettyLogger)
    private val javaAndMavenService = JavaAndMavenService(prettyLogger)
    private val pomFileService = PomFileService(prettyLogger)
    private val bundleService = BundleService(prettyLogger)
    private val jarFileService = JarFileServices(prettyLogger)
    private val gitService = GitService(prettyLogger)

    override fun run() {
        // 1) Exposes Java and Maven Version
        prettyLogger.printInfoMessage("Abaco CLI Deployer Tools")

        javaAndMavenService.evaluateMavenAndJavaVersions()

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

        val availableConfigurations = configurationService.getAvailableConfigurations()

        if (availableConfigurations == null) {
            prettyLogger.printErrorAndExitMessages("No configuration files found")
            return
        }

        prettyLogger.printInfoMessage("Currently available configuration files:")
        prettyLogger.printInfoMessage("")

        val maxIndex = availableConfigurations.size - 1

        availableConfigurations.forEachIndexed { index, fileName ->
            prettyLogger.printInfoMessage("[$index] $fileName")
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Choose a configuration (from 0 to $maxIndex):")
        prettyLogger.printInfoMessage("")

        prettyLogger.printInputTag()
        var fileIndex = readlnOrNull()?.toIntOrNull() ?: -1

        while (fileIndex < 0 || fileIndex > maxIndex) {
            prettyLogger.printWarnMessage("Wrong index. Choose a config file (from 0 to $maxIndex):")
            prettyLogger.printInputTag()
            fileIndex = readlnOrNull()?.toIntOrNull() ?: -1
        }

        val chosenFileName = availableConfigurations[fileIndex]

        prettyLogger.printInfoMessage("Selected file: $chosenFileName")
        prettyLogger.printInfoMessage("")

        val buildConfiguration = configurationService.getConfigurationByFileName(chosenFileName)

        if (buildConfiguration == null) {
            prettyLogger.printErrorAndExitMessages("Internal error. No configuration found with name: $chosenFileName")
            return
        }

        prettyLogger.printInfoMessage("Engine project:    ${buildConfiguration.engineProjectPath}")
        prettyLogger.printInfoMessage("Bundle project:    ${buildConfiguration.bundlesProjectPath}")
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

        val enginePomFile = Paths.get(buildConfiguration.engineProjectPath, buildConfiguration.pomFileName).toFile()

        if (!enginePomFile.exists()) {
            prettyLogger.printErrorAndExitMessages("pom file not found for engine project at ${buildConfiguration.engineProjectPath}")
            return
        }

        prettyLogger.printInfoMessage("Found main pom file")
        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Retrieving engine current version...")
        prettyLogger.printInfoMessage("")

        val currentEngineProjectVersion = pomFileService.scanPomFileForVersion(enginePomFile)

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

        mavenCommandsService.setNewVersionAndCommit(nextVersion, buildConfiguration.engineProjectPath)

        prettyLogger.printInfoMessage("")

        mavenCommandsService.buildJars(buildConfiguration.engineProjectPath)

        val createdBundlesWithoutVersions = mutableMapOf<String, String>()

        for (engineJavaModule in buildConfiguration.engineJavaModulesNames) {
            val targetBundleFolder = Paths.get(
                buildConfiguration.bundlesProjectPath,
                buildConfiguration.configFolderName,
                engineJavaModule.tenant,
                buildConfiguration.engineKind
            ).toFile()


            val newBundleName: String = if (createdBundlesWithoutVersions[targetBundleFolder.path] == null) {
                val newBundlePaddedVersion = bundleService
                    .getBundleNewPaddedSerialNumber(buildConfiguration.bundleName, targetBundleFolder)
                    ?: return

                createdBundlesWithoutVersions[targetBundleFolder.path] = newBundlePaddedVersion

                "${buildConfiguration.bundleName}-$newBundlePaddedVersion"
            } else {
                "${buildConfiguration.bundleName}-${createdBundlesWithoutVersions[targetBundleFolder.path]}"
            }

            val newBundleFolders = Paths
                .get(targetBundleFolder.path, newBundleName, buildConfiguration.bundleSubFolder).toFile()

            if (!newBundleFolders.exists()) {
                if (!newBundleFolders.mkdirs()) {
                    prettyLogger.printErrorAndExitMessages("Cannot create target bundle folders")
                    return
                }
            }

            val areJarFilesCorrectlyMoved = jarFileService.moveJarFilesToBundles(
                engineJavaModule.moduleName,
                buildConfiguration.engineProjectPath,
                newBundleFolders
            )

            if (!areJarFilesCorrectlyMoved) {
                return
            }

            prettyLogger.printInfoMessage("Done")
            prettyLogger.printInfoMessage("")
        }

        prettyLogger.printInfoMessage("")

        gitService.prepareReleaseBranchForEngine(nextVersion, buildConfiguration)
        gitService.syncRepositoryAndPrepareReleaseBranch(nextVersion, buildConfiguration)

        prettyLogger.printInfoMessage("Bundle ${buildConfiguration.bundleName} deployed successfully!!!")
    }

    private fun printExitMessage() {
        prettyLogger.printErrorAndExitMessages("You have chosen to exit the application")
    }
}