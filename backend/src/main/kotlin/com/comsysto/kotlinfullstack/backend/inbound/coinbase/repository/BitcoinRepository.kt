package com.comsysto.kotlinfullstack.backend.inbound.coinbase.repository

import com.comsysto.kotlinfullstack.api.crypto.ReactiveClient
import com.comsysto.kotlinfullstack.backend.inbound.CurrencyDataRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.Duration

@Profile("coinbase")
@Component
class BitcoinRepository(private val client: ReactiveClient,
                        @Value("\${coinbase.inbound.channel.throttle.btc}") val throttleValue: Long) : CurrencyDataRepository {

    override fun currencyKey(): String {
        return "BTC"
    }

    override fun dataStream(): Flux<BigDecimal> {
        return client
                .getSpotPrice(currencyKey())
                .map { BigDecimal(it.data.amount) }
                .delaySubscription(Duration.ofMillis(throttleValue))
                .repeat()
    }
}

