package com.nexis.trade.api;

import com.nexis.trade.trading.Side;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderRequest(
		@NotBlank @Pattern(regexp = "^[A-Za-z.]{1,10}$") String symbol,
		@NotNull Side side,
		@NotNull @Min(1) @Max(100_000) Integer quantity
) {
}
