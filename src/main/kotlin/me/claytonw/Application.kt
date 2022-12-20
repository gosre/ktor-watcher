package me.claytonw

import dev.inmo.krontab.buildSchedule
import dev.inmo.krontab.doInfinity
import io.ktor.server.application.*
import kotlinx.coroutines.launch

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    println("Started!")
}