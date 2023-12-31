package com.rexspecs

import java.io.File
import java.nio.file.Path

fun fileAsString(filePath: String) = File(filePath).readText()

fun writeFile(what: String, where: Path) = where.toFile().writeText(what)
