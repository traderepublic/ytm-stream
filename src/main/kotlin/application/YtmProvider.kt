package application

import application.ports.RedisPort
import domain.Isin
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Act as a bridge between the WebSocket connections and the Redis.
 * This class is responsible for managing the subscriptions and ensuring that a single Redis subscription
 * is created for each ISIN, no matter how many WebSocket connections are opened.
 * It also ensures that the Redis connection is closed when the last WebSocket connection is closed.
 */
class YtmProvider(
    private val redisPort: RedisPort,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val logger = KotlinLogging.logger {}

    private val isinFlows = ConcurrentHashMap<Isin, SharedFlow<String>>()
    private val isinSubscriptions = ConcurrentHashMap<Isin, AtomicInteger>()

    fun subscribe(isin: Isin): Flow<String> {
        logger.debug { "New WebSocket connection for $isin" }
        val flow = getFlow(isin)
        val subscriptions = isinSubscriptions[isin]!!.incrementAndGet()
        logger.debug { "Number of Subscriptions for $isin: $subscriptions" }
        return flow
    }

    fun unsubscribe(isin: Isin) {
        val subscriptions = isinSubscriptions[isin]?.decrementAndGet()
        logger.debug { "Number of Subscriptions for $isin: $subscriptions" }

        if (subscriptions == 0) {
            cleanUp(isin)
        }
    }

    fun closeAll() {
        logger.info { "Closing all WebSocket connections" }
        isinFlows.keys.forEach { cleanUp(it) }
    }

    private fun cleanUp(isin: Isin) {
        logger.debug { "Cleaning up $isin" }
        redisPort.unsubscribeYieldToMaturity(isin)
        isinSubscriptions.remove(isin)
        isinFlows.remove(isin)
    }

    private fun getFlow(isin: Isin): SharedFlow<String> {
        return isinFlows.computeIfAbsent(isin) {
            val flow = MutableSharedFlow<String>(replay = 1)
            isinSubscriptions[isin] = AtomicInteger(0)
            redisPort.getYieldToMaturity(isin)?.let {
                scope.launch {
                    flow.emit(it.toPlainString())
                }
            }
            redisPort.subscribeYieldToMaturity(isin) {
                scope.launch {
                    flow.emit(it.toPlainString())
                }
            }

            flow.asSharedFlow()
        }
    }
}