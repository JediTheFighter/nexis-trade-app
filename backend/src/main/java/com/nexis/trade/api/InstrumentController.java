package com.nexis.trade.api;

import com.nexis.trade.market.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

	private final MarketService marketService;

	public InstrumentController(MarketService marketService) {
		this.marketService = marketService;
	}

	@GetMapping
	public List<InstrumentResponse> list() {
		return marketService.list();
	}

	@GetMapping("/{symbol}/quote")
	public QuoteResponse quote(@PathVariable String symbol) {
		return marketService.quote(symbol);
	}
}
