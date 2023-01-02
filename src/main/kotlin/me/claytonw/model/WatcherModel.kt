@file:Suppress("unused")

package me.claytonw.model

import me.claytonw.watcher.WatcherStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WatcherTableModel(
    val id: Int,
    val name: String,
    val endpoint: String,
    val updateInterval: Int,
    var status: WatcherStatus = WatcherStatus.OPERATIONAL
)

data class WatcherDayTableModel(
    val watcherId: Int,
    val date: LocalDate,
    var downtime: Int = 0
)

data class WatcherThymeleafModel(
    val id: Int,
    val name: String,
    val status: String,
    val days: List<WatcherDayThymeleafModel>
)

class WatcherDayThymeleafModel(
    localDate: LocalDate,
    val exists: Boolean,
    val downtime: Int
) {

    val date: String = localDate.format(DateTimeFormatter.ISO_DATE)

}