package kafka

import application.config.YtmTopologyConfig
import application.ports.RedisPort
import domain.Isin
import domain.Quote
import domain.YieldToMaturity
import domain.YtmCalculationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Produced

/**
 * Builds the Kafka Streams topology for the YTM calculation.
 * The topology reads quotes from the input topic, calculates the YTM, and writes the result to the output topic.
 * It also publishes the YTM to Redis, so that WebSocket clients can subscribe to it.
 */
class YtmTopologyBuilder(
    private val config: YtmTopologyConfig,
    private val ytmCalculationService: YtmCalculationService,
    private val redisPort: RedisPort
) {
    private val logger = KotlinLogging.logger {}

    private val isinSerde = StringSerde()
    private val quoteSerde = BigDecimalSerde()
    private val ytmSerde = BigDecimalSerde()

    fun build(): Topology {
        val streamsBuilder = StreamsBuilder()
        streamsBuilder.streamQuotes()
            .observeQuotes()
            .calculateYtm()
            .observeYtm()
            .sinkToRedis()
            .sinkToKafka()

        return streamsBuilder.build()
    }

    private fun StreamsBuilder.streamQuotes(): KStream<Isin, Quote> {
        return stream(
            config.inputTopic,
            Consumed.`as`<Isin, Quote>("quotes-topic")
                .withKeySerde(isinSerde)
                .withValueSerde(quoteSerde)
        )
    }

    private fun KStream<Isin, Quote>.observeQuotes(): KStream<Isin, Quote> {
        return peek(
            { isin, quote ->
                logger.info { "Observed event: key=$isin, value=$quote" }
            },
            Named.`as`("observe-quotes")
        )
    }

    private fun KStream<Isin, Quote>.calculateYtm(): KStream<Isin, YieldToMaturity> {
        return mapValues(
            { isin, quote -> ytmCalculationService.calculateYtm(isin, quote) },
            Named.`as`("calculate-ytm")
        ).filter(
            { _, ytm -> ytm != null },
            Named.`as`("filter-out-if-ytm-is-null")
        ).mapValues(
            { _, ytm -> ytm!! },
            Named.`as`("convert-ytm-to-non-null")
        )
    }

    private fun KStream<Isin, YieldToMaturity>.observeYtm(): KStream<Isin, YieldToMaturity> {
        return peek(
            { isin, ytm ->
                logger.info { "Transformed event: key=$isin, value=$ytm" }
            },
            Named.`as`("observe-ytm")
        )
    }

    private fun KStream<Isin, YieldToMaturity>.sinkToRedis(): KStream<Isin, YieldToMaturity> {
        return peek(
            { isin, ytm ->
                redisPort.publishYieldToMaturity(isin, ytm)
            },
            Named.`as`("sink-to-redis")
        )
    }

    private fun KStream<Isin, YieldToMaturity>.sinkToKafka() {
        to(
            config.outputTopic,
            Produced
                .`as`<Isin, YieldToMaturity>("sink-to-ytm-topic")
                .withKeySerde(isinSerde)
                .withValueSerde(ytmSerde)
        )
    }


}