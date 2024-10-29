package com.uanitteiru.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun InputStream.readToString(): List<String> {
    val stdInput = BufferedReader(InputStreamReader(this))
    val result = mutableListOf<String>()

    var s: String?
    while ((stdInput.readLine().also { s = it }) != null) {
        result.add(s!!)
    }

    return result
}