package me.claytonw.plugin

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import me.claytonw.model.WatcherDayThymeleafModel
import me.claytonw.model.WatcherThymeleafModel
import me.claytonw.util.nameFormatted
import me.claytonw.watcher.WatcherDayTable
import me.claytonw.watcher.WatcherTable
import me.claytonw.watcher.config.WatcherConfiguration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun Application.configureRouting(config: WatcherConfiguration) {
    routing {

        static("/") {
            staticBasePackage = "static"
            resources(".") //serve all files recursively in base package
        }

        get("/") {
            val model = HashMap<String, Any>()

            //global variables
            model["display_period"] = config.displayPeriod

            //add watcher data
            val now = LocalDate.now(ZoneOffset.UTC)
            model["watchers"] = WatcherTable.getAll().associateBy({ it.id }, {watcherModel ->
                //grab previously recorded days and map to the number of days in the past each was from today
                val daysRecorded = WatcherDayTable.getAll(watcherModel.id, config.displayPeriod)
                    .associateBy({ChronoUnit.DAYS.between(it.date, now).toInt()}, {it})

                //create an array of fixed size days and add missing entries over the period
                val daysThymeleaf = Array(config.displayPeriod) { index ->
                    if (daysRecorded.containsKey(index)) {
                        val day = daysRecorded.getValue(index)
                        return@Array WatcherDayThymeleafModel(day.date, true, day.downtime)
                    } else {
                        return@Array WatcherDayThymeleafModel(now.minusDays(index.toLong()), false, 0)
                    }
                }

                return@associateBy WatcherThymeleafModel(
                    watcherModel.id,
                    watcherModel.name,
                    watcherModel.status.nameFormatted(),
                    daysThymeleaf.toList()
                )
            })

            call.respond(ThymeleafContent("index", model))
        }

    }
}