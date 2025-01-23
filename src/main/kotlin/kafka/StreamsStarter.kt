package kafka

import application.config.YtmTopologyConfig
import application.ports.RedisPort
import domain.YtmCalculationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology

private val logger = KotlinLogging.logger {}

fun startKafkaStreams(
    config: YtmTopologyConfig,
    ytmCalculationService: YtmCalculationService,
    redisPort: RedisPort
): KafkaStreams {
    val topology = buildTopology(config, ytmCalculationService, redisPort)
    val kafkaStreams = KafkaStreams(topology, config.kafkaStreamsProperties)
    kafkaStreams.start()
    Runtime.getRuntime().addShutdownHook(Thread(kafkaStreams::close))
    logger.info { "KafkaStreams started" }
    return kafkaStreams
}

private fun buildTopology(
    config: YtmTopologyConfig,
    ytmCalculationService: YtmCalculationService,
    redisPort: RedisPort
): Topology {
    val topology = YtmTopologyBuilder(config, ytmCalculationService, redisPort).build()
    val topologyDescription = topology.describe().toString()
    logger.info { topologyDescription }
    return topology
}