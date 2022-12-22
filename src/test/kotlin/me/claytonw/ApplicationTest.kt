package me.claytonw

import com.charleskorn.kaml.Yaml
import me.claytonw.watcher.config.WatcherConfiguration
import me.claytonw.watcher.config.WatcherTarget
import org.junit.Test

class ApplicationTest {

    @Test
    fun testKamlEncoding() {
        val targets = mutableListOf<WatcherTarget>()
        targets.add(WatcherTarget("Personal Website", "https://claytonw.me", "*/5 * * * *"))

        val config = WatcherConfiguration(targets)
        val result = Yaml.default.encodeToString(WatcherConfiguration.serializer(), config)
        println(result)
    }

}