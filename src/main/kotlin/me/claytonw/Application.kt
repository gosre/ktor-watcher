@file:Suppress("unused")

package me.claytonw

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.claytonw.plugins.configureThymeleaf
import me.claytonw.plugins.configureRouting
import me.claytonw.plugins.configureSerialization
import me.claytonw.watcher.Watcher
import me.claytonw.watcher.WatcherStatus
import me.claytonw.watcher.config.WatcherConfiguration
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.server() {
    configureThymeleaf()
    configureSerialization()
    configureRouting()
}

fun Application.watcher() {
    val client = HttpClient(CIO)
    val config = Yaml.default.decodeFromStream<WatcherConfiguration>(
        File("${System.getProperty("user.dir")}/src/main/resources/watcher.yaml").inputStream()
    )
    for (target in config.targets) {
        val watcher = Watcher(target)
        Watcher.watchers.add(watcher)
        launch {
            var currentInterval = target.interval
            while (true) {
                val start = System.currentTimeMillis()
                val response = client.request(target.host) {
                    method = HttpMethod.Get
                    //note: not any real performance gain in using head requests instead
                    //also some servers are not configured properly to respond to them
                }
                val elapsed = System.currentTimeMillis() - start
                val today = watcher.today()
                if (response.status == HttpStatusCode.OK) {
                    if (watcher.status == WatcherStatus.OFFLINE) {
                        //host was previously down but is now OK
                        log.debug("Host '${target.name}' was previously offline, but now online again.")
                        watcher.status = WatcherStatus.OPERATIONAL
                        currentInterval = target.interval
                    }
                } else {
                    if (watcher.status == WatcherStatus.OPERATIONAL) {
                        //host is now offline. Change state and check again at a faster interval
                        log.debug("Host '${target.name}' is now offline!")
                        watcher.status = WatcherStatus.OFFLINE
                        currentInterval = 1
                    } else {
                        log.debug("Host '${target.name}' remains offline.")
                    }
                    //increment down counter by 1 minute
                    today.downTimeMinutes++
                }
                log.debug("${target.name}: Elapsed=${elapsed}ms Status=${response.status}")
                delay(((currentInterval * 60_000L) - elapsed).coerceAtLeast(1))
            }
        }
    }
}