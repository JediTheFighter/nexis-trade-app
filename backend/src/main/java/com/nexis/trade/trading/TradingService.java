package com.nexis.trade.trading;

import com.nexis.trade.api.ApiException;
import com.nexis.trade.api.OrderRequest;
import com.nexis.trade.api.OrderResponse;
import com.nexis.trade.api.PortfolioResponse;
import com.nexis.trade.api.PositionResponse;
import com.nexis.trade.market.InstrumentRepository;
import com.nexis.trade.market.Quote;
import com.nexis.trade.market.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TradingService {

	private static final Logger log = LoggerFactory.getLogger(TradingService.class);

	private final AccountRepository accounts;
	private final OrderRepository orders;
	private final PositionRepository positions;
	private final QuoteRepository quotes;
	private final InstrumentRepository instruments;

	public TradingService(
			AccountRepository accounts,
			OrderRepository orders,
			PositionRepository positions,
			QuoteRepository quotes,
			InstrumentRepository instruments) {
		this.accounts = accounts;
		this.orders = orders;
		this.positions = positions;
		this.quotes = quotes;
		this.instruments = instruments;
	}

	@Transactional
	public OrderResponse placeOrder(UUID userId, OrderRequest request) {
		String symbol = request.symbol().trim().toUpperCase();
		if (!instruments.existsById(symbol)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "UNKNOWN_SYMBOL", "Unknown symbol " + symbol);
		}

		Account account = accounts.findByUserIdForUpdate(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_MISSING", "No paper account for this user."));
		Quote quote = quotes.findById(symbol)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "UNKNOWN_SYMBOL", "Unknown symbol " + symbol));

		BigDecimal fillPrice = quote.getPrice();
		BigDecimal notion = fillPrice.multiply(BigDecimal.valueOf(request.quantity())).setScale(2, RoundingMode.HALF_UP);
		BigDecimal realizedPnl = null;

		if (request.side() == Side.BUY) {
			if (account.getCashBalance().compareTo(notion) < 0) {
				throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_CASH", "Not enough cash for this buy.");
			}
			account.setCashBalance(account.getCashBalance().subtract(notion));
			addShares(account.getId(), symbol, request.quantity(), fillPrice);
		} else {
			Position position = positions.findByAccountIdAndSymbol(account.getId(), symbol)
					.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_SHARES", "No shares to sell."));
			if (position.getQuantity() < request.quantity()) {
				throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_SHARES", "Not enough shares to sell.");
			}
			realizedPnl = fillPrice.subtract(position.getAverageCost())
					.multiply(BigDecimal.valueOf(request.quantity()))
					.setScale(2, RoundingMode.HALF_UP);
			account.setCashBalance(account.getCashBalance().add(notion));
			int remaining = position.getQuantity() - request.quantity();
			if (remaining == 0) {
				positions.delete(position);
			} else {
				position.setQuantity(remaining);
			}
		}

		TradeOrder order = new TradeOrder(
				UUID.randomUUID(),
				account.getId(),
				symbol,
				request.side(),
				request.quantity(),
				OrderStatus.FILLED,
				fillPrice,
				realizedPnl,
				Instant.now());
		orders.save(order);
		log.info("Filled order {} account {} {} {} {}", order.getId(), account.getId(), request.side(), request.quantity(), symbol);
		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> orders(UUID userId) {
		Account account = requireAccount(userId);
		return orders.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PortfolioResponse portfolio(UUID userId) {
		Account account = requireAccount(userId);
		List<PositionResponse> rows = positions.findByAccountIdOrderBySymbolAsc(account.getId()).stream()
				.map(position -> toPosition(position))
				.toList();
		BigDecimal marketValue = rows.stream()
				.map(PositionResponse::marketValue)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal unrealized = rows.stream()
				.map(PositionResponse::unrealizedPnl)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new PortfolioResponse(
				account.getCashBalance(),
				account.getCashBalance().add(marketValue),
				unrealized,
				rows);
	}

	private void addShares(UUID accountId, String symbol, int quantity, BigDecimal fillPrice) {
		Position position = positions.findByAccountIdAndSymbol(accountId, symbol).orElse(null);
		if (position == null) {
			positions.save(new Position(UUID.randomUUID(), accountId, symbol, quantity, fillPrice));
			return;
		}
		BigDecimal oldQuantity = BigDecimal.valueOf(position.getQuantity());
		BigDecimal newQuantity = oldQuantity.add(BigDecimal.valueOf(quantity));
		BigDecimal totalCost = position.getAverageCost().multiply(oldQuantity).add(fillPrice.multiply(BigDecimal.valueOf(quantity)));
		position.setAverageCost(totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP));
		position.setQuantity(position.getQuantity() + quantity);
	}

	private PositionResponse toPosition(Position position) {
		Quote quote = quotes.findById(position.getSymbol()).orElseThrow();
		BigDecimal marketValue = quote.getPrice()
				.multiply(BigDecimal.valueOf(position.getQuantity()))
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal cost = position.getAverageCost()
				.multiply(BigDecimal.valueOf(position.getQuantity()))
				.setScale(2, RoundingMode.HALF_UP);
		String name = instruments.findById(position.getSymbol()).map(instrument -> instrument.getName()).orElse(position.getSymbol());
		return new PositionResponse(
				position.getSymbol(),
				name,
				position.getQuantity(),
				position.getAverageCost(),
				quote.getPrice(),
				marketValue,
				marketValue.subtract(cost));
	}

	private Account requireAccount(UUID userId) {
		return accounts.findByUserId(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_MISSING", "No paper account for this user."));
	}

	private OrderResponse toResponse(TradeOrder order) {
		return new OrderResponse(
				order.getId(),
				order.getSymbol(),
				order.getSide(),
				order.getQuantity(),
				order.getStatus().name(),
				order.getFillPrice(),
				order.getRealizedPnl(),
				order.getCreatedAt());
	}
}
