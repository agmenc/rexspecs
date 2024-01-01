package com.rexspecs.inputs

import com.rexspecs.utils.fileAsString
import org.jsoup.Jsoup

val sampleInput = """
            |<!doctype html>
            |<html lang="en">
            | <head>
            |  <meta charset="UTF-8">
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

fun sanified(filePath: String): String = htmlSanitised(fileAsString(filePath))

fun htmlSanitised(contents: String): String = Jsoup.parse(contents).html()