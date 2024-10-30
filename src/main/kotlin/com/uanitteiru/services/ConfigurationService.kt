package com.uanitteiru.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.uanitteiru.data.BuildConfiguration
import com.uanitteiru.utils.PrettyLogger
import java.nio.file.Paths

class ConfigurationService(private val prettyLogger: PrettyLogger) {

    fun getAvailableConfigurations() : List<String>? {
        // From where the project is started
        val path = System.getProperty("user.dir")

        val configurationsFolder = Paths.get(path, "bundle-deployer-configs").toFile()

        if (!configurationsFolder.exists()) {
            prettyLogger.printErrorAndExitMessages("Didn't found the configurations folder")
            return null
        }

        return configurationsFolder.listFiles()?.map { it.name }
    }

    fun getConfigurationByFileName(fileName: String) : BuildConfiguration? {
        val path = System.getProperty("user.dir")

        val configurationFile = Paths.get(path, "bundle-deployer-configs", fileName).toFile()

        if (!configurationFile.exists()) {
            return null
        }

        val objectMapper = jacksonObjectMapper()

        return objectMapper.readValue(configurationFile, BuildConfiguration::class.java)
    }
}