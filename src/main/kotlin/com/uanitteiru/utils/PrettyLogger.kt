package com.uanitteiru.utils

import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine.Help.Ansi

class PrettyLogger {
    init {
        AnsiConsole.systemInstall()
    }

    fun printInputTag() {
        AnsiConsole.out().print(Ansi.AUTO.string("[@|bold,green  IN |@] "))
    }

    fun printInfoMessage(message: String) {
        AnsiConsole.out().println(Ansi.AUTO.string("[@|bold,blue INFO|@] $message"))
    }

    fun printWarnMessage(message: String) {
        AnsiConsole.out().println(Ansi.AUTO.string("[@|bold,yellow WARN|@] $message"))
    }

    fun printErrorAndExitMessages(message: String) {
        printErrorMessage(message)
        printErrorMessage("")
        printErrorMessage("Exiting the application...")
    }

    private fun printErrorMessage(message: String) {
        AnsiConsole.out().println(Ansi.AUTO.string("[@|bold,red ERROR|@] $message"))
    }
}