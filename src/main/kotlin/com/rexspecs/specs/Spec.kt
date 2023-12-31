package com.rexspecs.specs

data class Spec(val identifier: String, val components: List<SpecComponent>)

interface SpecComponent

data class Title(val title: String): SpecComponent

class Ignorable: SpecComponent
