package me.claytonw.model

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

data class WatcherModel(
    val pk: String,
    val name: String,
    val days: List<WatcherDayModel>
)

class WatcherDayModel(
    localDate: LocalDate,
    val exists: Boolean,
    val downtime: Int
) {

    val date: String = localDate.toString(ISODateTimeFormat.date())

}