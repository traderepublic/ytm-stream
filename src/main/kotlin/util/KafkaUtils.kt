package util

import application.config.YtmTopologyConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import java.util.Optional
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * Some utilities to interact with Kafka and enable this demo on a single application.
 */
class KafkaUtils : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private var closed: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startQuotesProducerMock(producerProps: Properties, topic: String): QuotesProducerMock {
        val quotesProducerMock = QuotesProducerMock(producerProps, topic)
        scope.launch {
            quotesProducerMock.start()
        }
        return quotesProducerMock
    }

    fun createTopics(config: YtmTopologyConfig) {
        val topics = listOf(config.inputTopic, config.outputTopic)
            .map { topicName -> NewTopic(topicName, Optional.empty(), Optional.empty()) }
        AdminClient.create(config.kafkaStreamsProperties).use { adminClient ->
            val existingTopics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS)
            val topicsToCreate = topics.filter { it.name() !in existingTopics }

            if (topicsToCreate.isNotEmpty()) {
                adminClient.createTopics(topics).values().forEach { (topic, future) ->
                    try {
                        future.get()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to create topic: $topic" }
                    }
                }
            }

            val topicNames = topics.map { it.name() }
            logger.info { "Asking cluster for topic descriptions" }
            adminClient
                .describeTopics(topicNames)
                .allTopicNames()
                .get(10, TimeUnit.SECONDS)
                .forEach { (name, desc) ->
                    logger.info { "Topic: $name, details: $desc" }
                }
        }
    }

    override fun close() {
        if (!closed) {
            closed = true
            scope.cancel()
        }
    }
}

