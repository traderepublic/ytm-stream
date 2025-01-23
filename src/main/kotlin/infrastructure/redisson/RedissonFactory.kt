package infrastructure.redisson

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

fun createRedisClient(config: RedisConfig): RedissonClient {
    val maxThreads =
        (Runtime.getRuntime().availableProcessors().toDouble() / 4)
            .roundToInt()
            .coerceIn(1..4)
    val threadPool: ExecutorService = Executors.newFixedThreadPool(maxThreads)

    logger.info { "Connecting to Redis Cluster" }
    val redissonConfig = Config().apply {
        executor = threadPool
        threads = maxThreads
        nettyThreads = maxThreads * 2
        val clientConfig = useSingleServer()
            .setAddress(config.address)
            .setConnectionPoolSize(2)
            .setConnectionMinimumIdleSize(1)

        if (!config.password.isNullOrEmpty()) {
            clientConfig.password = config.password
        }
    }

    redissonConfig.codec = StringCodec.INSTANCE

    val client = Redisson.create(redissonConfig)
    logger.info { "Connected to Redis Cluster" }
    return client
}