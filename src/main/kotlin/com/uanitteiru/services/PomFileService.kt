package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger
import java.io.File

class PomFileService(private val prettyLogger: PrettyLogger) {

    fun scanPomFileForVersion(pomFile: File) : String? {
        val pomFileLines = pomFile.readLines()
        var version: String? = null

        for (pomFileLine in pomFileLines) {
            if (pomFileLine.trimStart().startsWith("<version>")) {
                version = pomFileLine.substringAfter("<version>").substringBefore("</version>")
                break
            }
        }

        return version
    }
}