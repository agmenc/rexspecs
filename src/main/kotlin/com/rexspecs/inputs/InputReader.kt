package com.rexspecs.inputs

import com.rexspecs.specs.Spec

/**
 * An InputReader knows where to find specs, and how to read them into their JSON representations
 */
interface InputReader {
    fun specs(): List<Spec>

    // Ensure that source location exists.
    fun prepareForInput()
}

