package util

import domain.Isin
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * Generates random bond prices for a given ISIN.
 * The price will change randomly between 70 and 120 with a random step size.
 */
data class RandomBondPriceGenerator(
    val isin: Isin
) {
    private var step = 0L
    private var startPrice = Random.nextInt(70, 120)
    private var targetPrice = Random.nextInt(70, 120)
    private var targetSteps = Random.nextInt(300)
    private var lastPrice = startPrice.toBigDecimal()

    private var stepWidth = 1.0 / targetSteps.toDouble()


    private fun reset() {
        startPrice = lastPrice.toInt()
        targetPrice = Random.nextInt(70, 120)
        targetSteps = Random.nextInt(300)
        stepWidth = 1.0 / targetSteps.toDouble()
        step = 0
    }

    fun nextPrice(): BigDecimal {
        if (step >= targetSteps) {
            reset()
        }
        step++
        val price = try {
            val minValue = lastPrice.toDouble() * -1 / 50
            val maxValue = lastPrice.toDouble() * 1 / 50
            (startPrice + ((targetPrice - startPrice) * stepWidth) * step).toBigDecimal() +
                    Random.nextDouble(minValue, maxValue).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
        } catch (e: Throwable) {
            e.printStackTrace()
            reset()
            BigDecimal.ZERO
        }
        lastPrice = if (price <= BigDecimal.ZERO) {
            BigDecimal.valueOf(0.01)
        } else {
            price
        }

        return lastPrice.setScale(4, RoundingMode.HALF_EVEN)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RandomBondPriceGenerator

        return isin == other.isin
    }

    override fun hashCode(): Int {
        return isin.hashCode()
    }
}