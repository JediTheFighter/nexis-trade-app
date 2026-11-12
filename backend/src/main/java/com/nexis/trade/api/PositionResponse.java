package com.nexis.trade.api;

import java.math.BigDecimal;

public record PositionResponse(
		String symbol,
		String name,
		int quantity,
		BigDecimal averageCost,
		BigDecimal lastPrice,
		BigDecimal marketValue,
		BigDecimal unrealizedPnl
) {
}
