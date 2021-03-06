package com.comsysto.kotlinfullstack.backend.subscription

import com.comsysto.kotlinfullstack.backend.CryptoStockServiceInterface
import java.time.ZonedDateTime
import java.util.*

internal class SubscriptionServiceImpl(private val repository: SubscriptionRepository,
                                       private val cryptoService: CryptoStockServiceInterface) :
        SubscriptionService,
        Map<String, CurrencyStockSubscription> by repository {

    override fun createSubscription(currencies: List<String>): CurrencyStockSubscription {
        val id = UUID.randomUUID().toString()
        val subscription = CurrencyStockSubscription.Pending(id, currencies, ZonedDateTime.now())
        repository.put(id, subscription)

        return subscription
    }

    override fun activateSubscription(sub: CurrencyStockSubscription.Pending): CurrencyStockSubscription.Active {
        val stream = cryptoService.currentPriceStream(sub.currencies)
        val activated = sub.activate(stream)
        repository.put(sub.id, activated)
        return activated
    }

    override fun changeSubscription(sub: CurrencyStockSubscription, currencies: List<String>): CurrencyStockSubscription {
        return when (sub) {
            is CurrencyStockSubscription.Active -> {
                val newCurrenciesStream = cryptoService.currentPriceStream(currencies)
                sub.stream.observableStream = newCurrenciesStream
                sub.stream.currencies = currencies
                sub
            }
            is CurrencyStockSubscription.Pending -> sub.changeCurrencies(currencies)
        }
    }
}