package me.claytonw.util.ext

fun Enum<*>.nameFormatted() = name
    .lowercase()
    .replace("_", " ")
    .replaceFirstChar { it.uppercase() }