package com.nexis.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Jwt jwt = new Jwt();
	private final Security security = new Security();
	private final Trading trading = new Trading();
	private final Market market = new Market();
	private final Cors cors = new Cors();

	public Jwt getJwt() {
		return jwt;
	}

	public Security getSecurity() {
		return security;
	}

	public Trading getTrading() {
		return trading;
	}

	public Market getMarket() {
		return market;
	}

	public Cors getCors() {
		return cors;
	}

	public static class Jwt {
		private String secret;
		private long expirationHours = 24;

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public long getExpirationHours() {
			return expirationHours;
		}

		public void setExpirationHours(long expirationHours) {
			this.expirationHours = expirationHours;
		}
	}

	public static class Security {
		private int bcryptStrength = 10;

		public int getBcryptStrength() {
			return bcryptStrength;
		}

		public void setBcryptStrength(int bcryptStrength) {
			this.bcryptStrength = bcryptStrength;
		}
	}

	public static class Trading {
		private BigDecimal startingCash = new BigDecimal("100000.00");

		public BigDecimal getStartingCash() {
			return startingCash;
		}

		public void setStartingCash(BigDecimal startingCash) {
			this.startingCash = startingCash;
		}
	}

	public static class Market {
		private boolean enabled = true;
		private long tickMs = 5000;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public long getTickMs() {
			return tickMs;
		}

		public void setTickMs(long tickMs) {
			this.tickMs = tickMs;
		}
	}

	public static class Cors {
		private String allowedOrigins = "http://localhost:5173";

		public String getAllowedOrigins() {
			return allowedOrigins;
		}

		public void setAllowedOrigins(String allowedOrigins) {
			this.allowedOrigins = allowedOrigins;
		}
	}
}
