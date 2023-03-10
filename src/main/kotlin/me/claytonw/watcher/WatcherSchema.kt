package me.claytonw.watcher

import me.claytonw.model.WatcherDayTableModel
import me.claytonw.model.WatcherTableModel
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.ZoneOffset

object WatcherTable : IntIdTable("watcher") {
    private val name = varchar("name", 50)
    private val endpoint = varchar("endpoint", 100)
    private val updateInterval = integer("update_interval")
    private val status = customEnumeration(
        name = "status",
        fromDb = { value -> WatcherStatus.valueOf(value as String) },
        toDb = { it.name }
    )

    fun getAll(): List<WatcherTableModel> = transaction {
        selectAll().map { watcherRow ->
            toModel(watcherRow)
        }
    }

    fun update(watcher: WatcherTableModel) = transaction {
        update({ WatcherTable.id eq watcher.id }) {
            it[status] = watcher.status
        }
    }

    private fun toModel(row: ResultRow): WatcherTableModel =
        WatcherTableModel(
            row[WatcherTable.id].value,
            row[name],
            row[endpoint],
            row[updateInterval],
            row[status]
        )
}

object WatcherDayTable : Table("watcher_day") {
    private val watcherId = reference("watcher_id", WatcherTable.id)
    private val date = date("date")
    private val downtime =  integer("downtime")

    fun getAll(watcherId: Int, limit: Int): List<WatcherDayTableModel> = transaction {
        WatcherDayTable
            .select { WatcherDayTable.watcherId eq watcherId }
            .orderBy(date to SortOrder.DESC)
            .limit(limit)
            .map { toModel(it) }
    }

    fun getOrInsert(watcherId: Int) = transaction {
        val now = LocalDate.now(ZoneOffset.UTC)
        val row = select { WatcherDayTable.watcherId eq watcherId and (date eq now)}.singleOrNull()
        if (row == null) {
            val day = WatcherDayTableModel(watcherId, now)
            insert {
                it[WatcherDayTable.watcherId] = watcherId
                it[date] = day.date
            }
            return@transaction day
        }
        return@transaction toModel(row)
    }

    fun update(model: WatcherDayTableModel) = transaction {
        update({ watcherId eq model.watcherId and (date eq model.date) }) {
            it[downtime] = model.downtime
        }
    }

    private fun toModel(row: ResultRow): WatcherDayTableModel =
        WatcherDayTableModel(
            row[watcherId].value,
            row[date],
            row[downtime]
        )
}

