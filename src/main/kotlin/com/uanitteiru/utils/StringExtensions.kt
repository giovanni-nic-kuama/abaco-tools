package com.uanitteiru.utils

fun String.toPowerShellCommand(): String {
    return "powershell.exe $this"
}