package com.nexis.trade.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "quotes")
public class Quote {

	@Id
	@Column(length = 10)
	private String symbol;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal price;

	@Column(name = "as_of", nullable = false)
	private Instant asOf;

	protected Quote() {
	}

	public String getSymbol() {
		return symbol;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Instant getAsOf() {
		return asOf;
	}

	public void setAsOf(Instant asOf) {
		this.asOf = asOf;
	}
}
