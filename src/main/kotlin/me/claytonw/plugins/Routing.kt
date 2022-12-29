package me.claytonw.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import me.claytonw.watcher.Watcher

fun Application.configureRouting() {
    routing {

        static("/") {
            staticBasePackage = "static"
            resources(".") //serve all files recursively in base package
            //defaultResource("index.html")
        }

        get("/") {
            val model = HashMap<String, Any>()
            model["day_count"] = Watcher.STORED_HISTORY
            model["watchers"] = Watcher.watchers.map { it.model() }
            call.respond(ThymeleafContent("index", model))
        }

        get("/watchers/") {
            call.respond(HttpStatusCode.OK, Watcher.watchers)
        }

    }
}
