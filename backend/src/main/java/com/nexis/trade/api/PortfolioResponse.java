package com.nexis.trade.api;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
		BigDecimal cash,
		BigDecimal equity,
		BigDecimal unrealizedPnl,
		List<PositionResponse> positions
) {
}
