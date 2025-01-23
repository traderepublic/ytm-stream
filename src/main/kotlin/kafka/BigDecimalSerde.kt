package kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import java.math.BigDecimal

class BigDecimalSerializer : Serializer<BigDecimal?> {
    override fun serialize(topic: String?, data: BigDecimal?): ByteArray {
        return data.toString().toByteArray()
    }
}

class BigDecimalDeserializer : Deserializer<BigDecimal?> {
    override fun deserialize(topic: String?, data: ByteArray?): BigDecimal? {
        return data?.let { BigDecimal(String(it)) }
    }
}

class BigDecimalSerde : Serde<BigDecimal?> {
    override fun serializer(): Serializer<BigDecimal?> {
        return BigDecimalSerializer()
    }

    override fun deserializer(): Deserializer<BigDecimal?> {
        return BigDecimalDeserializer()
    }
}