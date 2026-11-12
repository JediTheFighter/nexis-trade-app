package com.nexis.trade.api;

import com.nexis.trade.trading.TradingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

	private final TradingService tradingService;

	public PortfolioController(TradingService tradingService) {
		this.tradingService = tradingService;
	}

	@GetMapping
	public PortfolioResponse portfolio() {
		return tradingService.portfolio(CurrentUser.id());
	}
}
