package com.rexspecs.specs

import com.rexspecs.TestRow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Spec(val identifier: String, val components: List<SpecComponent>)

@Serializable
sealed class SpecComponent

@Serializable
@SerialName("com.rexspecs.TabularTest")
data class TabularTest(
    val fixtureName: String?,
    val inputColumns: List<String>,
    val expectationColumns: List<String>,
    val expectationsForAllRows: List<TestRow>
): SpecComponent()

@Serializable
data class Title(val title: String): SpecComponent()

// TODO: Support titled headings separately.
@Serializable
data class Heading(val words: String): SpecComponent()

@Serializable
data class Description(val words: String): SpecComponent()

@Serializable
class Ignorable: SpecComponent()
