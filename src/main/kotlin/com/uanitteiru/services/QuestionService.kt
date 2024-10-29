package com.uanitteiru.services

import com.uanitteiru.utils.PrettyLogger

class QuestionService(private val prettyLogger: PrettyLogger) {

    fun askPositiveOrNegativeQuestionQuestion(question: String) : Boolean {
        prettyLogger.printInfoMessage(question)
        prettyLogger.printInfoMessage("")
        prettyLogger.printInputTag()

        var confirmOrDismissJavaAndMavenVersion: String? = readlnOrNull()

        while (confirmOrDismissJavaAndMavenVersion != "y" && confirmOrDismissJavaAndMavenVersion != "n") {
            confirmOrDismissJavaAndMavenVersion = readlnOrNull()
        }

        return confirmOrDismissJavaAndMavenVersion == "y"
    }

    fun askNextVersionQuestion() : String? {
        prettyLogger.printInfoMessage("Choose the next version: ")
        prettyLogger.printInputTag()

        val nextVersion : String? = readlnOrNull()

        return nextVersion
    }
}