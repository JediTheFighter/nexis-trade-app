package com.nexis.trade.trading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class TradeOrder {

	@Id
	private UUID id;

	@Column(name = "account_id", nullable = false)
	private UUID accountId;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private Side side;

	@Column(nullable = false)
	private int quantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OrderStatus status;

	@Column(name = "fill_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal fillPrice;

	@Column(name = "realized_pnl", precision = 19, scale = 2)
	private BigDecimal realizedPnl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TradeOrder() {
	}

	public TradeOrder(
			UUID id,
			UUID accountId,
			String symbol,
			Side side,
			int quantity,
			OrderStatus status,
			BigDecimal fillPrice,
			BigDecimal realizedPnl,
			Instant createdAt) {
		this.id = id;
		this.accountId = accountId;
		this.symbol = symbol;
		this.side = side;
		this.quantity = quantity;
		this.status = status;
		this.fillPrice = fillPrice;
		this.realizedPnl = realizedPnl;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public String getSymbol() {
		return symbol;
	}

	public Side getSide() {
		return side;
	}

	public int getQuantity() {
		return quantity;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public BigDecimal getFillPrice() {
		return fillPrice;
	}

	public BigDecimal getRealizedPnl() {
		return realizedPnl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
