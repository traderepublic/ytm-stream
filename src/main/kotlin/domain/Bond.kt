package domain

import org.javamoney.moneta.Money

data class Bond(
    val isin: Isin,
    val couponPaymentAmount: Money,
    val faceAmount: Money,
    val numberOfYearsToMaturity: Int
)