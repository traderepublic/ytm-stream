package application

import adapters.InstrumentsAdapter
import adapters.RedisAdapter
import application.plugins.configureSockets
import application.ports.RedisPort
import domain.YtmCalculationService
import infrastructure.redisson.createRedisClient
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import kafka.startKafkaStreams
import util.KafkaUtils

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.mainModule() {
    val ytmTopologyConfig = ytmTopologyConfig()

    val kafkaUtils = KafkaUtils()
    val instrumentsAdapter = InstrumentsAdapter()
    val ytmCalculationService = YtmCalculationService(instrumentsAdapter)
    val redisPort = createRedisPort()
    Runtime.getRuntime().addShutdownHook(Thread(kafkaUtils::close))
    kafkaUtils.createTopics(ytmTopologyConfig)
    kafkaUtils.startQuotesProducerMock(ytmTopologyConfig.kafkaStreamsProperties, ytmTopologyConfig.inputTopic)

    startKafkaStreams(ytmTopologyConfig, ytmCalculationService, redisPort)

    val ytmProvider = YtmProvider(redisPort)
    configureSockets(ytmProvider)
}

fun Application.createRedisPort(): RedisPort {
    val redisClient = createRedisClient(redisConfig())
    return RedisAdapter(redisClient)
}