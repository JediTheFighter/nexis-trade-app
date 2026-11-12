package com.nexis.trade.api;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(String symbol, BigDecimal price, Instant asOf) {
}
