package com.nexis.trade.market;

import com.nexis.trade.api.ApiException;
import com.nexis.trade.api.InstrumentResponse;
import com.nexis.trade.api.QuoteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketService {

	private final InstrumentRepository instruments;
	private final QuoteRepository quotes;

	public MarketService(InstrumentRepository instruments, QuoteRepository quotes) {
		this.instruments = instruments;
		this.quotes = quotes;
	}

	@Transactional(readOnly = true)
	public List<InstrumentResponse> list() {
		return instruments.findAllByOrderBySymbolAsc().stream()
				.map(instrument -> {
					Quote quote = requireQuote(instrument.getSymbol());
					return new InstrumentResponse(instrument.getSymbol(), instrument.getName(), quote.getPrice(), quote.getAsOf());
				})
				.toList();
	}

	@Transactional(readOnly = true)
	public QuoteResponse quote(String symbol) {
		String normalized = symbol.trim().toUpperCase();
		Quote quote = quotes.findById(normalized)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "UNKNOWN_SYMBOL", "Unknown symbol " + normalized));
		return new QuoteResponse(quote.getSymbol(), quote.getPrice(), quote.getAsOf());
	}

	public Quote requireQuote(String symbol) {
		return quotes.findById(symbol)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "UNKNOWN_SYMBOL", "Unknown symbol " + symbol));
	}
}
