package me.claytonw.util

fun Enum<*>.nameFormatted() = name
    .lowercase()
    .replace("_", " ")
    .replaceFirstChar { it.uppercase() }