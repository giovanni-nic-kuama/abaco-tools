package com.uanitteiru.commands

import com.uanitteiru.utils.PrettyLogger
import org.eclipse.microprofile.config.inject.ConfigProperty
import picocli.CommandLine.Command
import java.io.File
import java.nio.file.Paths

@Command(name = "kube-deployer", mixinStandardHelpOptions = true)
class KubeDeployer : Runnable {
    @ConfigProperty(name = "kubernetes.path")
    lateinit var kubernetesConfigFolderPath: String

    override fun run() {
        val prettyLogger = PrettyLogger()

        prettyLogger.printInfoMessage("Kube Config Deployer")
        prettyLogger.printInfoMessage("")

        val kubernetesFolder = Paths.get(kubernetesConfigFolderPath).toFile()

        if (!kubernetesFolder.exists()) {
            prettyLogger.printErrorAndExitMessages("Kubernetes folder not found")
            return
        }

        val kubernetesFolderFiles = kubernetesFolder.listFiles()
                ?.filter { it.isFile }
                ?.filter { !it.name.equals("config") }

        if (kubernetesFolderFiles.isNullOrEmpty()) {
            prettyLogger.printErrorAndExitMessages("No config files found on Kubernetes folder")
            return
        }

        val maxIndex = kubernetesFolderFiles.size - 1

        prettyLogger.printInfoMessage("Currently available Kubernetes config files:")
        prettyLogger.printInfoMessage("")

        kubernetesFolderFiles.forEachIndexed { index, file ->
            prettyLogger.printInfoMessage("[$index] ${file.name}")
        }

        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Choose a config file to deploy (from 0 to $maxIndex):")
        prettyLogger.printInfoMessage("")

        prettyLogger.printInputTag()
        var fileIndex = readlnOrNull()?.toIntOrNull() ?: -1

        while (fileIndex < 0 || fileIndex > maxIndex) {
            prettyLogger.printWarnMessage("Wrong index. Choose a config file to deploy (from 0 to $maxIndex):")
            prettyLogger.printInputTag()
            fileIndex = readlnOrNull()?.toIntOrNull() ?: -1
        }

        val chosenFile = kubernetesFolderFiles[fileIndex]
        prettyLogger.printInfoMessage("Choosen file: ${chosenFile.name}")
        prettyLogger.printInfoMessage("")
        prettyLogger.printInfoMessage("Deploying ${chosenFile.name}...")
        prettyLogger.printInfoMessage("")

        val outputFile = File(kubernetesFolder, "config")
        chosenFile.copyTo(outputFile, true)

        prettyLogger.printInfoMessage("Successfully deployed ${chosenFile.name}! Happy K9s")
        prettyLogger.printInfoMessage("")

        return
    }
}