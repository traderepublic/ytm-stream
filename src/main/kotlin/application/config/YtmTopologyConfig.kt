package application.config

import java.util.Properties

/**
 * Configuration for our Kafka Streams topology.
 * @property inputTopic The topic from which data will be consumed.
 * @property outputTopic The topic to which data will be produced.
 * @property kafkaStreamsProperties The Kafka Streams properties.
 */
data class YtmTopologyConfig(
    val inputTopic: String,
    val outputTopic: String,
    val kafkaStreamsProperties: Properties
)
