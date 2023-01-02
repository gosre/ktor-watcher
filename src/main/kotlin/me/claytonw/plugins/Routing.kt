package me.claytonw.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import me.claytonw.model.WatcherDayThymeleafModel
import me.claytonw.model.WatcherThymeleafModel
import me.claytonw.util.ext.nameFormatted
import me.claytonw.watcher.WatcherDayTable
import me.claytonw.watcher.WatcherTable
import java.time.LocalDate
import java.time.ZoneOffset

fun Application.configureRouting() {
    routing {

        static("/") {
            staticBasePackage = "static"
            resources(".") //serve all files recursively in base package
            //defaultResource("index.html")
        }

        get("/") {
            val displayPeriodDays = 90

            val model = HashMap<String, Any>()
            model["day_count"] = displayPeriodDays

            val watchers = WatcherTable.getAll().map { watcherModel ->
                val daysRecorded = WatcherDayTable.getAll(watcherModel.id).toMutableList()
                val daysThymeleaf = daysRecorded.map { dayModel ->
                    WatcherDayThymeleafModel(dayModel.date, true, dayModel.downtime)
                }.toMutableList()
                //add filler entries for missing days over the last period
                if (daysRecorded.size < displayPeriodDays) {
                    val mostRecent: LocalDate = if (daysRecorded.isEmpty()) {
                        LocalDate.now(ZoneOffset.UTC)
                    } else {
                        daysRecorded.maxOf { it.date }
                    }
                    for (i in 1L..(displayPeriodDays - daysRecorded.size)) {
                        daysThymeleaf.add(WatcherDayThymeleafModel(mostRecent.minusDays(i), false, 0))
                    }
                }
                return@map WatcherThymeleafModel(
                    watcherModel.id,
                    watcherModel.name,
                    watcherModel.status.nameFormatted(),
                    daysThymeleaf
                )
            }
            model["watchers"] = watchers

            call.respond(ThymeleafContent("index", model))
        }

        get("/watchers/") {
            //call.respond(HttpStatusCode.OK, Watcher.watchers)
        }

    }
}
