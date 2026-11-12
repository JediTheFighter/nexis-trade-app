package com.nexis.trade.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instruments")
public class Instrument {

	@Id
	@Column(length = 10)
	private String symbol;

	@Column(nullable = false, length = 80)
	private String name;

	protected Instrument() {
	}

	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}
}
