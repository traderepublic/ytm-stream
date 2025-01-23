package application

import application.config.YtmTopologyConfig
import infrastructure.redisson.RedisConfig
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import java.util.Properties

fun Application.ytmTopologyConfig(): YtmTopologyConfig {
    val ytmTopologyConfig = environment.config.config("ytmTopologyConfig")
    return YtmTopologyConfig(
        inputTopic = ytmTopologyConfig.property("inputTopic").getString(),
        outputTopic = ytmTopologyConfig.property("outputTopic").getString(),
        kafkaStreamsProperties = ytmTopologyConfig.config("kafkaStreamsProperties").toProperties(),
    )
}

fun Application.redisConfig(): RedisConfig {
    val redisConfig = environment.config.config("redisProperties")
    return RedisConfig(redisConfig.toProperties())
}

fun ApplicationConfig.toProperties(): Properties {
    return toMap().mapValues { it.value.toString() }.toProperties()
}