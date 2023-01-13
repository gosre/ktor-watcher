package me.claytonw.watcher.config

import kotlinx.serialization.Serializable

@Serializable
data class WatcherConfiguration(
    val host: String = "0.0.0.0",
    val port: Int = 8080,
    val displayPeriod: Int = 90,
    val mysql: MySQLConfiguration
)

@Serializable
data class MySQLConfiguration(
    val host: String,
    val port: Int = 3306,
    val username: String,
    val password: String,
    val database: String
) {

    fun toConnectorString() = "jdbc:mysql://$username:$password@$host:$port/$database"

}