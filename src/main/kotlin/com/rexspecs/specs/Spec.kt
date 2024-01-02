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
data class TabularTest(val fixtureName: String, val columnNames: List<String>, val testRows: List<TestRow>): SpecComponent()

@Serializable
data class Title(val title: String): SpecComponent()

@Serializable
class Ignorable: SpecComponent()
