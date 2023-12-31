package com.rexspecs

import org.jsoup.Jsoup
import java.io.File

fun fileAsString(filePath: String) = File(filePath).readText()

fun writeFile(what: String, where: String) = File(where).writeText(what)
