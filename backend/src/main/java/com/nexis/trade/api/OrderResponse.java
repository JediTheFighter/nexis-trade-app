package com.nexis.trade.api;

import com.nexis.trade.trading.Side;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
		UUID id,
		String symbol,
		Side side,
		int quantity,
		String status,
		BigDecimal fillPrice,
		BigDecimal realizedPnl,
		Instant createdAt
) {
}
