package com.rexspecs.utils

const val ANSI_RESET: String = "\u001B[0m"
const val ANSI_RED: String = "\u001B[31m"
const val ANSI_GREEN: String = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"

fun failed(message: String) = printColour(ANSI_RED, message)
fun succeeded(message: String) = printColour(ANSI_GREEN, message)
fun errored(message: String) = printColour(ANSI_YELLOW, message)

private fun printColour(colour: String, message: String) = println("$colour${message}${ANSI_RESET}")