package com.uanitteiru.commands

import com.uanitteiru.utils.toPowerShellCommand
import picocli.CommandLine.Command
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

@Command(name = "bundle-deployer", mixinStandardHelpOptions = true)
class BundleDeployer : Runnable {
    private val engineProjectPath = "C:\\Users\\Noitu\\Devel\\abaco\\fvg-payment-prints"
    private val engineProjectName = "liquidation-list"
    private val bundlesProjectPath = "C:\\Users\\Noitu\\Devel\\abaco\\agri-bundles"
    private val bundlePath = "config"
    private val tenant = "master"
    private val engineName = "print-engine"
    private val bundleName = "appspay-proc"
    private val bundleSubFolder = "print-flow"

    override fun run() {
        // 1) Exposes Java and Maven Version
        println("\n=== Abaco CLI Deployer Tools ===\n")

        val javaVersionOutput = Runtime.getRuntime().exec("java --version".toPowerShellCommand())
            .inputStream.bufferedReader().readText()

        val mavenVersionOutput = Runtime.getRuntime().exec("mvn --version".toPowerShellCommand())
            .inputStream.bufferedReader().readText()

        println("=== Java version for current session ===\n")
        println(javaVersionOutput)

        println("=== Maven version for current session ===\n")
        println(mavenVersionOutput)


        // 2) Ask user if maven and java are correct.
        println("> Do you want to continue? [y/n]")
        var confirmOrDismissJavaAndMavenVersion: String? = readlnOrNull()

        while (confirmOrDismissJavaAndMavenVersion != "y" && confirmOrDismissJavaAndMavenVersion != "n") {
            confirmOrDismissJavaAndMavenVersion = readlnOrNull()
        }

        if (confirmOrDismissJavaAndMavenVersion == "n") {
            return
        }

        println("\n=== Loaded Projects: ===\n")

        println("Engine:    $engineProjectPath")
        println("Bundles:   $bundlesProjectPath")

        println("\n > Are loaded projects correct? [y/n]")

        var confirmOrDismissProjectPaths: String? = readlnOrNull()

        while (confirmOrDismissProjectPaths != "y" && confirmOrDismissProjectPaths != "n") {
            confirmOrDismissProjectPaths = readlnOrNull()
        }

        if (confirmOrDismissProjectPaths == "n") {
            return
        }

        println("\n=== Scanning engine pom file... ===")

        val enginePomFile = Paths.get(engineProjectPath, "pom.xml").toFile()

        if (!enginePomFile.exists()) {
            println("Error: pom file not found for engine project at $engineProjectPath")
            return
        }

        println("\n=== pom file found ===\n")
        println("=== Scanning project version... ===\n")
        var version : String? = null

        val pomFileLines = enginePomFile.readLines()

        for (pomFileLine in pomFileLines) {
            if (pomFileLine.trimStart().startsWith("<version>")) {
                version = pomFileLine.substringAfter("<version>").substringBefore("</version>")
                break
            }
        }

        if (version == null) {
            println("=== No project version found ===")
            return
        }

        println("=== Project version has been found! ===")
        println("Found version: $version\n")

        println("=== Choose the next version: ===")
        val nextVersion : String? = readlnOrNull()

        if (nextVersion == null) {
            println("Error setting the new version")
            return
        }

        println("=== Version $nextVersion will be set on pom files! ===")
        println("\n > Are you sure? [y/n]")

        var confirmOrDismissNewVersion: String? = readlnOrNull()

        while (confirmOrDismissNewVersion != "y" && confirmOrDismissNewVersion != "n") {
            confirmOrDismissNewVersion = readlnOrNull()
        }

        if (confirmOrDismissNewVersion == "n") {
            return
        }

        val runtime = Runtime.getRuntime()

        println("\n=== Setting version $nextVersion on project! ===\n")
        runtime.exec("mvn versions:set -DnewVersion=''$nextVersion''".toPowerShellCommand(), null, File(engineProjectPath))
            .waitFor()

        runtime.exec("mvn versions:commit".toPowerShellCommand(), null, File(engineProjectPath))
            .waitFor()

        println("=== Building jars ===\n")

        val proc = runtime.exec("mvn clean package -DskipTests".toPowerShellCommand(), null, File(engineProjectPath))

        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

        // Read the output from the command
        var outputLockEnabled = true
        var s: String? = null
        while ((stdInput.readLine().also { s = it }) != null) {
            if (s?.contains("Reactor Summary for") == true) outputLockEnabled = false

            if (!outputLockEnabled) {
                println(s)
            }
        }

        println("=== Jar built ===\n")
//
//        // TODO: modules name from pom
        val moduleTargetFolderFileList = Paths.get("$engineProjectPath/$engineProjectName", "target")
            .toFile()
            .listFiles()?.toList() ?: emptyList()

        val jarFiles = moduleTargetFolderFileList
            .filter { it.name.endsWith(".jar") }
            .filter { !it.name.contains("original") }
            .toList()

        if (jarFiles.isEmpty() || jarFiles.size > 1) {
            println("=== Didn't find the correct jar ===\n")
            return
        }

        val jarFile = jarFiles[0]

        println("=== Jar $jarFile is ready! ===\n")

        val bundleFolder = Paths.get(bundlesProjectPath, bundlePath, tenant, engineName).toFile()

        if (!bundleFolder.exists()) {
            println("Error. Target Folder Bundle not found at $bundleFolder")
        }

        var previousBundles = bundleFolder.listFiles()
            ?.filter { it.isDirectory }
            ?.filter { it.name.contains(bundleName) }
            ?: listOf()

        if (previousBundles.isEmpty()) {
            println("Error. Bundle with name $bundleName does not exist")
            return
        }

        previousBundles = previousBundles.sortedByDescending { it.name }

        val latestBundle = previousBundles[0]

        val splitBundleName = latestBundle.name.split("-")

        if (splitBundleName.size != 3) {
            println("Error. Cannot extract bundle version from file")
            return
        }

        val latestBundleVersion = splitBundleName[2]

        val latestBundleVersionInt = latestBundleVersion.toIntOrNull()

        if (latestBundleVersionInt == null) {
            println("Cannot convert latest bundle version to a Number")
            return
        }

        val paddedBundleVersion = (latestBundleVersionInt + 1).toString().padStart(4, '0')
        val newBundleName = "$bundleName-$paddedBundleVersion"

        val newBundleFolders = Paths.get(bundleFolder.path, newBundleName, bundleSubFolder).toFile()

        if (!newBundleFolders.mkdirs()) {
            println("Cannot create folders")
            return
        }

        val targetJarFile = File(newBundleFolders, jarFile.name)

        jarFile.copyTo(targetJarFile, true)

        val branchName = "test/bundle-deployer"
        val commitMessage = "this is a test of deploying ${jarFile.name}"

        runtime.exec("git fetch --all --prune --prune-tags".toPowerShellCommand(), null, File(bundlesProjectPath))
            .waitFor()

        runtime.exec("git checkout master".toPowerShellCommand(), null, File(bundlesProjectPath))
            .waitFor()

        runtime.exec("git pull origin master".toPowerShellCommand(), null, File(bundlesProjectPath))
            .waitFor()

        runtime.exec("git switch -c $branchName".toPowerShellCommand(), null, File(bundlesProjectPath))
            .waitFor()

        runtime.exec("git add .", null, File(bundlesProjectPath))
            .waitFor()

        runtime.exec(listOf("powershell.exe", "git", "commit", "-m", "'$commitMessage'").toTypedArray(), null, File(bundlesProjectPath))

        val shouldPush = false
    }
}