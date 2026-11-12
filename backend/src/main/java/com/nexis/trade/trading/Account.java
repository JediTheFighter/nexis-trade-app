package com.nexis.trade.trading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false, unique = true)
	private UUID userId;

	@Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal cashBalance;

	protected Account() {
	}

	public Account(UUID id, UUID userId, BigDecimal cashBalance) {
		this.id = id;
		this.userId = userId;
		this.cashBalance = cashBalance;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public BigDecimal getCashBalance() {
		return cashBalance;
	}

	public void setCashBalance(BigDecimal cashBalance) {
		this.cashBalance = cashBalance;
	}
}
