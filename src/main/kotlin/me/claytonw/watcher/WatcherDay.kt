package me.claytonw.watcher

import org.joda.time.DateTimeZone
import org.joda.time.LocalDate

class WatcherDay(
    val date: LocalDate = LocalDate.now(DateTimeZone.UTC)
) {

    var downTime: Int = 0

}