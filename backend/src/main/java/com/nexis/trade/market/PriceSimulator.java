package com.nexis.trade.market;

import com.nexis.trade.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(prefix = "app.market", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PriceSimulator {

	private static final Logger log = LoggerFactory.getLogger(PriceSimulator.class);

	private final QuoteRepository quotes;

	public PriceSimulator(QuoteRepository quotes, AppProperties properties) {
		this.quotes = quotes;
		log.info("Price simulator ticks every {} ms", properties.getMarket().getTickMs());
	}

	@Scheduled(fixedDelayString = "${app.market.tick-ms:5000}")
	@Transactional
	public void tick() {
		Instant now = Instant.now();
		for (Quote quote : quotes.findAll()) {
			quote.setPrice(PriceWalk.nextPrice(quote.getPrice(), ThreadLocalRandom.current().nextDouble()));
			quote.setAsOf(now);
		}
	}
}
