package domain

import adapters.InstrumentsAdapter
import nl.hiddewieringa.money.ofCurrency
import org.javamoney.moneta.Money
import java.math.BigDecimal
import org.javamoney.calc.securities.YieldToMaturity as YtmCalculator

class YtmCalculationService(
    private val instrumentsAdapter: InstrumentsAdapter
) {

    /**
     * Calculate the Yield to Maturity (YTM) for a given ISIN and quote.
     */
    fun calculateYtm(isin: Isin, quote: Quote): YieldToMaturity? {
        val bond = instrumentsAdapter.getInstrument(isin) ?: return null

        val absolutePercentage = quote.divide(100.toBigDecimal())
        val faceAmount = BigDecimal.valueOf(bond.faceAmount.number.doubleValueExact())
        val price = (absolutePercentage * faceAmount).ofCurrency<Money>("EUR")

        return YtmCalculator.calculate(
            bond.couponPaymentAmount,
            bond.faceAmount,
            price,
            bond.numberOfYearsToMaturity
        )
    }
}