package me.claytonw.watcher.config

import kotlinx.serialization.Serializable

@Serializable
data class WatcherConfiguration(val mysql: MySQLConfiguration)

@Serializable
data class MySQLConfiguration(
    val host: String,
    val port: Int = 3306,
    val username: String,
    val password: String,
    val database: String
)