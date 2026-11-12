package com.nexis.trade.market;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceWalkTest {

	@Test
	void movesPriceWithinHalfAPercent() {
		BigDecimal last = new BigDecimal("100.0000");
		BigDecimal up = PriceWalk.nextPrice(last, 0.99);
		BigDecimal down = PriceWalk.nextPrice(last, 0.01);
		assertThat(up).isBetween(new BigDecimal("100.0000"), new BigDecimal("100.5000"));
		assertThat(down).isBetween(new BigDecimal("99.5000"), new BigDecimal("100.0000"));
	}

	@Test
	void doesNotFallThroughTheFloor() {
		assertThat(PriceWalk.nextPrice(new BigDecimal("0.0100"), 0.0)).isEqualByComparingTo("0.0100");
	}
}
