package com.rexspecs.inputs

import com.rexspecs.utils.fileAsString
import org.jsoup.Jsoup

val sampleInput = """
            |<!doctype html>
            |<html lang="en">
            | <head>
            |  <meta charset="UTF-8">
            |  <link rel="stylesheet" href="../theme.css">
            |  <title>An Acceptance Test</title>
            | </head>
            | <body>
            |  <table>
            |   <thead>
            |    <tr>
            |     <th>Calculator</th>
            |    </tr>
            |    <tr>
            |     <th>First Param</th>
            |     <th>Operator</th>
            |     <th>Second Param</th>
            |     <th>HTTP Response</th>
            |     <th>Result</th>
            |    </tr>
            |   </thead>
            |   <tbody>
            |    <tr>
            |     <td>7</td>
            |     <td>+</td>
            |     <td>8</td>
            |     <td>200</td>
            |     <td>15</td>
            |    </tr>
            |    <tr>
            |     <td>7</td>
            |     <td>x</td>
            |     <td>8</td>
            |     <td>201</td>
            |     <td>56</td>
            |    </tr>
            |   </tbody>
            |  </table>
            | </body>
            |</html>
        """.trimMargin()

val expectedOutputWithFailure = firstRowSucceeds(sampleInput)
    .replace("<td>56</td>", "<td class=\"fail\">Expected [56] but was: [Unsupported operator: \"x\"]</td>")
    .replace("<td>201</td>", "<td class=\"fail\">Expected [201] but was: [400]</td>")

val expectedOutputWithSuccess = firstRowSucceeds(sampleInput)
    .replace("<td>56</td>", "<td class=\"success\">56</td>")
    .replace("<td>201</td>", "<td class=\"success\">201</td>")

private fun firstRowSucceeds(input: String) = input
    .replace("<td>15</td>", "<td class=\"success\">15</td>")
    .replace("<td>200</td>", "<td class=\"success\">200</td>")

fun sanified(filePath: String): String = htmlSanitised(fileAsString(filePath))

fun htmlSanitised(contents: String): String = Jsoup.parse(contents).html()