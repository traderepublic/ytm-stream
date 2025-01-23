package adapters

import domain.Bond
import domain.Isin
import nl.hiddewieringa.money.ofCurrency

/**
 * This class is a mock implementation of an adapter that would fetch instruments from a database or a remote service.
 * For the sake of simplicity, we are using a hardcoded list of instruments.
 */
class InstrumentsAdapter {

    private val instrumentsMap = listOf(
        Bond("AT0000A1XML2", 21.ofCurrency("EUR"), 1000.ofCurrency("EUR"), 93),
        Bond("BE0000324336", 4.5.ofCurrency("EUR"), 100.ofCurrency("EUR"), 1),
        Bond("BE6276040431", 15.ofCurrency("EUR"), 1000.ofCurrency("EUR"), 5),
        Bond("DE0001102440", 0.5.ofCurrency("EUR"), 100.ofCurrency("EUR"), 3),
        Bond("DE0001135366", 4.75.ofCurrency("EUR"), 100.ofCurrency("EUR"), 16),
        Bond("IT0005534141", 45.ofCurrency("EUR"), 1000.ofCurrency("EUR"), 29),
        Bond("XS2441552192", 12.5.ofCurrency("EUR"), 1000.ofCurrency("EUR"), 5),
    ).associateBy { it.isin }

    fun getAvailableIsins(): Set<Isin> = instrumentsMap.keys

    fun getInstrument(isin: String): Bond? {
        return instrumentsMap[isin]
    }
}