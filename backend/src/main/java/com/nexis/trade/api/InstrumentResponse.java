package com.nexis.trade.api;

import java.math.BigDecimal;
import java.time.Instant;

public record InstrumentResponse(String symbol, String name, BigDecimal price, Instant asOf) {
}
