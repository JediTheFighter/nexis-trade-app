package com.nexis.trade.trading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "symbol"}))
public class Position {

	@Id
	private UUID id;

	@Column(name = "account_id", nullable = false)
	private UUID accountId;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
	private BigDecimal averageCost;

	protected Position() {
	}

	public Position(UUID id, UUID accountId, String symbol, int quantity, BigDecimal averageCost) {
		this.id = id;
		this.accountId = accountId;
		this.symbol = symbol;
		this.quantity = quantity;
		this.averageCost = averageCost;
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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getAverageCost() {
		return averageCost;
	}

	public void setAverageCost(BigDecimal averageCost) {
		this.averageCost = averageCost;
	}
}
