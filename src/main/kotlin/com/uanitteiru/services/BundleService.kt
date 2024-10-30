package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import java.io.File

class BundleService(private val prettyLogger: PrettyLogger) {

    fun getBundleNewPaddedSerialNumber(bundleName: String, targetBundleFolder: File) : String? {
        // Bundle Search
        if (!targetBundleFolder.exists()) {
            prettyLogger.printErrorAndExitMessages("Target Bundle folder [$targetBundleFolder] not found")
        }

        var previousBundles = targetBundleFolder.listFiles()
            ?.filter { it.isDirectory }
            ?.filter { it.name.contains(bundleName) }
            ?: listOf()

        if (previousBundles.isEmpty()) {
            prettyLogger.printErrorAndExitMessages("Bundle with name $bundleName does not exist")
            return null
        }

        previousBundles = previousBundles.sortedByDescending { it.name }

        // Latest bundle for bundleName
        val latestBundleFolder = previousBundles[0]

        // Extract bundle serial number (eg: 0000, 0001)
        val latestBundleSplitName = latestBundleFolder.name.split("-")

        val latestBundleSerialNumber = latestBundleSplitName[latestBundleSplitName.size - 1]

        val latestBundleSerialNumberAsInt = latestBundleSerialNumber.toIntOrNull()

        if (latestBundleSerialNumberAsInt == null) {
            prettyLogger.printErrorAndExitMessages("Could not extract serial number for bundle $bundleName")
            return null
        }

        val paddedBundleVersion = (latestBundleSerialNumberAsInt + 1).toString().padStart(4, '0')

        return paddedBundleVersion
    }
}