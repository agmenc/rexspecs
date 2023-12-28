package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec
import com.rexspecs.output
import com.rexspecs.writeFile
import java.io.File

interface OutputWriter {
    fun writeSpecResults(executedSpec: ExecutedSpec, filePath: String)
    fun cleanTargetDir()
}

open class FileOutputWriter(private val testSourceRoot: String) : OutputWriter {
    // TODO: move filePath into ExecutedSpec
    override fun writeSpecResults(executedSpec: ExecutedSpec, filePath: String) {
        writeFile(executedSpec.output(), filePath)
    }

    override fun cleanTargetDir() {
        File(testSourceRoot).listFiles()?.forEach {
            val didItWork = it.delete()
            println("Deleted ${it.absolutePath} ==> ${didItWork}")
        }
    }
}