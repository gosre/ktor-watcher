@file:Suppress("unused")

package me.claytonw

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.claytonw.plugins.configureRouting
import me.claytonw.plugins.configureSerialization
import me.claytonw.plugins.configureThymeleaf
import me.claytonw.watcher.WatcherDayTable
import me.claytonw.watcher.WatcherStatus
import me.claytonw.watcher.WatcherTable
import me.claytonw.watcher.config.WatcherConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.SQLException
import kotlin.system.exitProcess

fun main() {
    val logger = LoggerFactory.getLogger(Application::class.java)
    val configStream = Application::class.java.classLoader.getResourceAsStream("watcher.yaml")
    if (configStream == null) {
        logger.error("Could not find watcher configuration. Exiting...")
        exitProcess(2)
    }
    val config = Yaml.default.decodeFromStream<WatcherConfiguration>(configStream)
    embeddedServer(Netty, host = config.host, port = config.port) {
        server(config)
        watcher(config)
    }.start(wait = true)
}

fun Application.server(config: WatcherConfiguration) {
    configureThymeleaf()
    configureSerialization()
    configureRouting(config)
}

fun Application.watcher(config: WatcherConfiguration) {
    val client = HttpClient(CIO)

    val mysql = config.mysql
    Database.connect(config.mysql.toConnectorString() + "?serverTimezone=UTC")

    transaction {
        try {
            if (!WatcherTable.exists() || !WatcherDayTable.exists()) {
                log.error("Schema `${mysql.database}` does not have the proper tables to continue.")
                exitProcess(2)
            }
        } catch (e: SQLException) {
            log.error("MySQL connectivity issue, please check configuration file and database permissions.")
            e.printStackTrace()
            exitProcess(2)
        }
    }

    launch {
        WatcherTable.getAll().forEach { watcher ->
            val name = watcher.name
            launch {
                var currentInterval = watcher.updateInterval
                while (true) {
                    val start = System.currentTimeMillis()
                    val response = client.request(watcher.endpoint) {
                        method = HttpMethod.Get
                        //note: not any real performance gain in using head requests instead
                        //also some servers are not configured properly to respond to them
                    }
                    val today = WatcherDayTable.getToday(watcher.id)
                    if (response.status == HttpStatusCode.OK) {
                        if (watcher.status == WatcherStatus.OFFLINE) {
                            //host was previously down but is now OK
                            log.debug("Host '$name' was previously offline, but now online again.")
                            watcher.status = WatcherStatus.OPERATIONAL
                            currentInterval = watcher.updateInterval
                            WatcherTable.update(watcher)
                        }
                    } else {
                        if (watcher.status == WatcherStatus.OPERATIONAL) {
                            //host is now offline. Change state and check again at a faster interval
                            log.debug("Host '$name' is now offline!")
                            watcher.status = WatcherStatus.OFFLINE
                            currentInterval = 1
                            WatcherTable.update(watcher)
                        } else {
                            log.debug("Host '$name' remains offline.")
                        }
                        //increment down counter by 1 minute
                        today.downtime++
                        WatcherDayTable.update(today)
                    }
                    val elapsed = System.currentTimeMillis() - start
                    log.debug("$name: Elapsed=${elapsed}ms Status=${response.status}")
                    delay(((currentInterval * 60_000L) - elapsed).coerceAtLeast(1))
                }
            }
        }
    }
}