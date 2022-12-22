package me.claytonw.watcher

import me.claytonw.watcher.config.WatcherTarget

class Watcher(val target: WatcherTarget) {

    val recordings = ArrayList<WatcherRecording>()

    companion object {

        val watchers = ArrayList<Watcher>()

    }

}