package com.nexis.trade.api;

import com.nexis.trade.trading.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final TradingService tradingService;

	public OrderController(TradingService tradingService) {
		this.tradingService = tradingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse place(@Valid @RequestBody OrderRequest request) {
		return tradingService.placeOrder(CurrentUser.id(), request);
	}

	@GetMapping
	public List<OrderResponse> list() {
		return tradingService.orders(CurrentUser.id());
	}
}
