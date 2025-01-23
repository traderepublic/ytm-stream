package adapters

import application.ports.RedisPort
import domain.Isin
import domain.YieldToMaturity
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.collections.ConcurrentMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.redisson.api.RedissonClient

/**
 * Adapter for Redis.
 * This class provides the communication between the application and the Redis, serializing and deserializing the data
 * before it is stored, retrieved, published and consumer.
 */
class RedisAdapter(
    private val redisClient: RedissonClient
) : RedisPort {
    private val logger = KotlinLogging.logger {}

    private val subscriptions = ConcurrentMap<Isin, Int>()

    override fun getYieldToMaturity(isin: Isin): YieldToMaturity? {
        val redisMap = get(isin)
        return if (redisMap.isNullOrEmpty()) {
            null
        } else {
            YieldToMaturity(redisMap["ytm"]!!)
        }
    }

    override fun publishYieldToMaturity(isin: Isin, ytm: YieldToMaturity) {
        val redisMap = mapOf("ytm" to ytm.toPlainString())
        publish(isin, redisMap)
        store(isin, redisMap)
    }

    override fun subscribeYieldToMaturity(isin: Isin, callback: (YieldToMaturity) -> Unit) {
        subscribe(isin) { message ->
            callback(YieldToMaturity(message["ytm"]!!))
        }
    }

    override fun unsubscribeYieldToMaturity(isin: Isin) {
        subscriptions[isin]?.let {
            val topic = redisClient.getTopic(isin)
            topic.removeListener(it)
        }
        subscriptions.remove(isin)
    }

    private fun get(isin: Isin): Map<String, String>? {
        return redisClient.getMap<String?, String?>(isin).readAllMap()
    }

    private fun store(isin: Isin, values: Map<String, String>) {
        val rMap = redisClient.getMap<String?, String?>(isin)
        try {
            rMap.putAll(values)
        } catch (e: Exception) {
            logger.error { "Redis error storing object to bucket: $isin" }
        }
    }

    private fun publish(isin: Isin, message: Map<String, String>) {
        val encodedMap = Json.encodeToString(message)
        val topic = redisClient.getTopic(isin)
        try {
            val numSubscribers = topic.publish(encodedMap)
            logger.debug { "Message published successfully for $isin, number of subscribers: $numSubscribers" }
        } catch (e: Exception) {
            logger.error { "Redis error publishing message to topic: $isin" }
        }
    }

    private fun subscribe(isin: Isin, callback: (Map<String, String>) -> Unit) {
        subscriptions.computeIfAbsent(isin) {
            val topic = redisClient.getTopic(isin)
            topic.addListenerAsync(String::class.java) { _, message ->
                val decodedMap = Json.decodeFromString<Map<String, String>>(message)
                callback(decodedMap)
            }.get()
        }
    }
}