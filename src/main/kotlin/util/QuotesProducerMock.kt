package util

import adapters.InstrumentsAdapter
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import domain.Isin
import domain.Quote
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

/**
 * A mock Kafka producer that sends random bond prices to a Kafka topic.
 * In a real-world scenario, this would be replaced by a real-time data feed.
 */
class QuotesProducerMock(
    private val producerProps: Properties,
    private val topic: String,
    private val instrumentsAdapter: InstrumentsAdapter = InstrumentsAdapter()
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private var closed: Boolean = false

    init {
        this.producerProps.setProperty("client.id", "quotes-producer-mock")
    }

    suspend fun start() {
        try {
            KafkaProducer<Isin, Quote>(producerProps).use { producer ->
                val generators = instrumentsAdapter.getAvailableIsins().map { isin -> RandomBondPriceGenerator(isin) }
                while (!closed) {
                    try {
                        generators.forEach { generator ->
                            producer.send(ProducerRecord(topic, generator.isin, generator.nextPrice())).get()
                        }
                        delay(5000)
                    } catch (e: InterruptedException) {
                        logger.error(e) { "Failed to send message" }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to create Kafka producer" }
        }
    }

    override fun close() {
        closed = true
    }
}