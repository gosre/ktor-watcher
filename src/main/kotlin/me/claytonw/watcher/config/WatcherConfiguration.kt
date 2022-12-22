package me.claytonw.watcher.config

import kotlinx.serialization.Serializable

@Serializable
data class WatcherConfiguration(val targets: List<WatcherTarget>)

@Serializable
data class WatcherTarget(val name: String, val host: String, val interval: String)