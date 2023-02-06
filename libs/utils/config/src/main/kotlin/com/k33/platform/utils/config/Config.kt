package com.k33.platform.utils.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

private val logger by lazy { LoggerFactory.getLogger("com.k33.platform.utils.config.Config") }

fun getConfig(
    name: String,
    path: String? = null,
): Lazy<Config> = lazy {
    getConfigEager(name, path)
}

inline fun <reified CONFIG : Any> loadConfig(
    name: String,
    path: String? = null,
): Lazy<CONFIG> = lazy {
    loadConfigEager(name, path)
}

inline fun <reified CONFIG : Any> loadConfigEager(
    name: String,
    path: String? = null,
): CONFIG = getConfigEager(name, path).extract()

fun getConfigEager(
    name: String,
    path: String? = null,
): Config {
    val configFile = ConfigAsResourceFile("/$name.conf")
    if (configFile.exists()) {
        logger.info("Loading config: $configFile")
        var config = ConfigFactory.parseString(configFile.readText())
        if (!path.isNullOrBlank()) {
            config = config.resolve().getConfig(path)
        }
        return config
    }
    throw FileNotFoundException("Config file not found - $configFile")
}

data class ConfigAsResourceFile(
    private val name: String,
) {
    fun exists(): Boolean = object {}.javaClass.getResource(name) != null
    override fun toString(): String = "resource:$name @ ${object {}.javaClass.getResource(name)?.file}"
    fun readText(): String? = object {}.javaClass.getResource(name)?.readText()
}