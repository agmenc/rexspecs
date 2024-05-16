package com.rexspecs.utils

import kotlinx.serialization.Serializable

@Serializable
sealed class Either<out L, out R> {
    // TODO - implement map(), mapLeft() and mapRight()

    @Serializable
    data class Left<out L>(val left: L) : Either<L, Nothing>()

    @Serializable
    data class Right<out R>(val right: R) : Either<Nothing, R>()
}

fun <L, R, LL, RR> Either<L, R>.mapBoth(leftOp: (L) -> LL, rightOp: (R) -> RR): Either<LL, RR> = when (this) {
    is Either.Left -> Either.Left(leftOp(left))
    is Either.Right -> Either.Right(rightOp(right))
}

fun <I> identity(i: I): I = i

// TODO - Combine and simplify assumeLeft and assumeRight
fun <L, R> assumeLeft(value: Either<L, R>?): L =
    when (value) {
        is Either.Left -> value.left
        // TODO - log type information for L and R
        else -> throw RuntimeException("Expected Either.Left, but was ${value}")
    }

// TODO - Combine and simplify assumeLeft and assumeRight
fun <L, R> assumeRight(value: Either<L, R>?): R =
    when (value) {
        is Either.Right -> value.right
        // TODO - log type information for L and R
        else -> throw RuntimeException("Expected Either.Right, but was ${value}")
    }

// TODO - Make this typesafe and not awful
fun <T> lefts(inputs: Map<String, Either<String, T>>): Map<String, Either.Left<String>> {
    return inputs.filter { (_, v) -> v is Either.Left<String> } as Map<String, Either.Left<String>>
}

/** Add a pair to a nullable [map], creating a new map if necessary. */
operator fun <K,V> Pair<K, V>.plus(map: Map<K, V>?): Map<K, V> = map?.plus(this) ?: mapOf(this)

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.debugged() = also {
    println(it)
}

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.printed() = also(::println)

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.printed(blah: String) = also { println(blah) }

// Playing with some chaining that allows better fluidity that the Elvis operator
fun <T,U> T?.andThen(nullableBlock: (T) -> U?): U? {
    return if (this == null) null
    else nullableBlock(this)
}

fun <T> T?.orElse(nullableBlock: () -> T?): T? {
    return this ?: nullableBlock()
}