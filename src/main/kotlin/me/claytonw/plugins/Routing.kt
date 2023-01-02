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
import java.time.temporal.ChronoUnit

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

            //global variables
            model["day_count"] = displayPeriodDays

            //add watcher data
            val now = LocalDate.now(ZoneOffset.UTC)
            val watchers = WatcherTable.getAll().map { watcherModel ->
                //grab previously recorded days and map to the number of days in the past each was from today
                val daysRecorded = WatcherDayTable.getAll(watcherModel.id, displayPeriodDays)
                    .associateBy({ChronoUnit.DAYS.between(it.date, now).toInt()}, {it})

                //create an array of fixed size days and add missing entries over the period
                val daysThymeleaf = Array(displayPeriodDays) { index ->
                    if (daysRecorded.containsKey(index)) {
                        val day = daysRecorded.getValue(index)
                        return@Array WatcherDayThymeleafModel(day.date, true, day.downtime)
                    } else {
                        return@Array WatcherDayThymeleafModel(now.minusDays(index.toLong()), false, 0)
                    }
                }

                return@map WatcherThymeleafModel(
                    watcherModel.id,
                    watcherModel.name,
                    watcherModel.status.nameFormatted(),
                    daysThymeleaf.toList()
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