package com.nexis.trade.market;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceWalk {

	public static final BigDecimal FLOOR = new BigDecimal("0.0100");

	private PriceWalk() {
	}

	/**
	 * Moves a price by up to half a percent. {@code unitRandom} is in the range [0, 1).
	 */
	public static BigDecimal nextPrice(BigDecimal last, double unitRandom) {
		double shock = (unitRandom - 0.5) * 0.01;
		BigDecimal next = last.multiply(BigDecimal.valueOf(1.0 + shock)).setScale(4, RoundingMode.HALF_UP);
		if (next.compareTo(FLOOR) < 0) {
			return FLOOR;
		}
		return next;
	}
}
