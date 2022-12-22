package me.claytonw.watcher

import io.ktor.http.*
import org.joda.time.DateTime

class WatcherRecording(val status: HttpStatusCode, val timestamp: DateTime = DateTime.now()) {

}