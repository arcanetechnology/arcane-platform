package no.arcane.platform.utils.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

private val logger by lazy { LoggerFactory.getLogger("no.arcane.platform.utils.config.Config") }

fun getConfig(
    name: String,
    path: String = "",
    env: String = System.getenv("ENV") ?: "gcp",
): Lazy<Config> = lazy {
    getConfigEager(name, path, env)
}

inline fun <reified CONFIG : Any> loadConfig(
    name: String,
    path: String = "",
    env: String = System.getenv("ENV") ?: "gcp",
): Lazy<CONFIG> = lazy {
    getConfigEager(name, path, env).extract()
}

fun getConfigEager(
    name: String,
    path: String,
    env: String,
): Config {
    val configPriorityList = listOf(
        ConfigAsFile("/config/$name-$env.conf"),
        ConfigAsResourceFile("/$name-$env.conf"),
        ConfigAsResourceFile("/$name.conf"),
    )
    for (configFile in configPriorityList) {
        if (configFile.exists()) {
            logger.info("Loading config: $configFile")
            return ConfigFactory.parseString(configFile.readText()).resolve().getConfig(path)
        }
    }
    throw FileNotFoundException("Config files not found for env: $env - $configPriorityList")
}

sealed class ConfigFile(
    open val name: String,
) {
    abstract fun exists(): Boolean
    abstract fun readText(): String?
}

data class ConfigAsFile(
    val filePath: String,
) : ConfigFile(name = filePath) {
    override fun exists(): Boolean = File(filePath).exists()
    override fun readText(): String? = File(filePath).readText()
    override fun toString(): String = "file:$filePath"
}

data class ConfigAsResourceFile(
    override val name: String,
) : ConfigFile(name) {
    override fun exists(): Boolean = object {}.javaClass.getResource(name) != null
    override fun toString(): String = "resource:$name @ ${object {}.javaClass.getResource(name)?.file}"
    override fun readText(): String? = object {}.javaClass.getResource(name)?.readText()
}