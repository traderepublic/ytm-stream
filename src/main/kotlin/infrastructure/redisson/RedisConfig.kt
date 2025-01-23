package infrastructure.redisson

import java.util.Properties

data class RedisConfig(
    val address: String,
    val password: String?
) {
    constructor(properties: Properties) : this(
        address = properties.getProperty("address")!!,
        password = properties.getProperty("password")
    )
}
