package me.claytonw.plugins

import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.claytonw.watcher.Watcher

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("watchers" to Watcher.watchers)))
        }
    }
}
