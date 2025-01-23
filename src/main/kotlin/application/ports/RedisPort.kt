package application.ports

import domain.Isin
import domain.YieldToMaturity

interface RedisPort {

    /**
     * Get the Yield to Maturity for a given ISIN.
     * If no Yield to Maturity is found, return null.
     */
    fun getYieldToMaturity(isin: Isin): YieldToMaturity?

    /**
     * Publish the Yield to Maturity for a given ISIN.
     * This will update the value in Redis and notify all subscribers.
     */
    fun publishYieldToMaturity(isin: Isin, ytm: YieldToMaturity)

    /**
     * Subscribe to Yield to Maturity updates for a given ISIN.
     * The callback will be called every time the Yield to Maturity is updated.
     */
    fun subscribeYieldToMaturity(isin: Isin, callback: (YieldToMaturity) -> Unit)

    /**
     * Unsubscribe from Yield to Maturity updates for a given ISIN.
     * This will remove the callback from the list of subscribers.
     */
    fun unsubscribeYieldToMaturity(isin: Isin)
}