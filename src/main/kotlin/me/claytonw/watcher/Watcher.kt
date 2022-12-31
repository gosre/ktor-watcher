package me.claytonw.watcher

import me.claytonw.model.WatcherDayModel
import me.claytonw.model.WatcherModel
import me.claytonw.util.ext.nameFormatted
import me.claytonw.watcher.config.WatcherTarget
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import java.util.LinkedList

class Watcher(private val target: WatcherTarget) {

    var status: WatcherStatus = WatcherStatus.OPERATIONAL

    private val days = LinkedList<WatcherDay>()

    fun today(): WatcherDay {
        val now = LocalDate.now(DateTimeZone.UTC)
        for (day in days) {
            if (day.date.isEqual(now)) {
                return day
            }
        }
        val watcher = WatcherDay(now)
        days.addFirst(watcher)
        if (days.size > STORED_HISTORY) {
            days.removeLast()
        }
        return watcher
    }

    /**
     * Constructs a model from this watcher to be used in a view
     */
    fun model(): WatcherModel {
        val daysFilled = days
            .map { WatcherDayModel(it.date, true, it.downTimeMinutes) }
            .toMutableList()
        var last: LocalDate
        for (i in 1..(STORED_HISTORY - daysFilled.size)) {
            last = days.lastOrNull()?.date ?: LocalDate.now(DateTimeZone.UTC).plusDays(1)
            daysFilled.add(WatcherDayModel(last.minusDays(i), false, 0))
        }
        return WatcherModel("${target.name.hashCode()}", target.name, status.nameFormatted(), daysFilled)
    }

    companion object {

        const val STORED_HISTORY = 90
        val watchers = ArrayList<Watcher>()

    }

}