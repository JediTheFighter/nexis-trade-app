package com.nexis.trade;

import com.nexis.trade.market.Quote;
import com.nexis.trade.market.QuoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TradingApiTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private QuoteRepository quotes;

	@AfterEach
	void resetQuotes() {
		reset("AAPL", "190.0000");
		reset("MSFT", "420.0000");
	}

	@Test
	void listsSeededInstrumentsWithQuotes() throws Exception {
		mockMvc.perform(get("/api/instruments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(10)))
				.andExpect(jsonPath("$[?(@.symbol == 'AAPL')].price").value(190.0));
	}

	@Test
	void returnsLatestQuote() throws Exception {
		mockMvc.perform(get("/api/instruments/aapl/quote"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.symbol").value("AAPL"))
				.andExpect(jsonPath("$.price").value(190.0));
	}

	@Test
	void unknownSymbolQuoteIsNotFound() throws Exception {
		mockMvc.perform(get("/api/instruments/NOPE/quote"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("UNKNOWN_SYMBOL"));
	}

	@Test
	void registerAndLogin() throws Exception {
		String email = email("ada");
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(email, "paper-trade")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.email").value(email));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(email, "wrong-password")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(email, "paper-trade")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
	}

	@Test
	void portfolioRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/portfolio"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@Test
	void buyThenSellUpdatesCashPositionAndRealizedPnl() throws Exception {
		String token = register("buyer");

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "BUY", 10)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("FILLED"))
				.andExpect(jsonPath("$.fillPrice").value(190.0));

		mockMvc.perform(get("/api/portfolio").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cash").value(98100.0))
				.andExpect(jsonPath("$.positions", hasSize(1)))
				.andExpect(jsonPath("$.positions[0].quantity").value(10))
				.andExpect(jsonPath("$.positions[0].averageCost").value(190.0))
				.andExpect(jsonPath("$.unrealizedPnl").value(0.0));

		Quote quote = quotes.findById("AAPL").orElseThrow();
		quote.setPrice(new BigDecimal("200.0000"));
		quote.setAsOf(Instant.now());
		quotes.saveAndFlush(quote);

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "SELL", 10)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.realizedPnl").value(100.0));

		mockMvc.perform(get("/api/portfolio").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cash").value(100100.0))
				.andExpect(jsonPath("$.positions", hasSize(0)));

		mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void rejectsOverspendOversellAndUnknownSymbol() throws Exception {
		String token = register("limits");

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "BUY", 1000)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_CASH"));

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "BUY", 1)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "SELL", 2)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_SHARES"));

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("NOPE", "BUY", 1)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("UNKNOWN_SYMBOL"));
	}

	@Test
	void oneUserCannotSeeAnotherUsersPortfolio() throws Exception {
		String first = register("first");
		String second = register("second");

		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + first)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("MSFT", "BUY", 1)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/portfolio").header("Authorization", "Bearer " + second))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cash").value(100000.0))
				.andExpect(jsonPath("$.positions", hasSize(0)));

		mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + second))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void concurrentBuysCannotOverspendTheSameCash() throws Exception {
		String token = register("race");
		Callable<Integer> buy = () -> mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "BUY", 400)))
				.andReturn()
				.getResponse()
				.getStatus();

		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> first = pool.submit(buy);
			Future<Integer> second = pool.submit(buy);
			assertThat(java.util.List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
					.containsExactlyInAnyOrder(201, 422);
		} finally {
			pool.shutdownNow();
		}

		mockMvc.perform(get("/api/portfolio").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cash").value(24000.0))
				.andExpect(jsonPath("$.positions[0].quantity").value(400));
	}

	@Test
	void invalidOrderIsRejectedBeforeItReachesTheBook() throws Exception {
		String token = register("invalid");
		mockMvc.perform(post("/api/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(order("AAPL", "BUY", 0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("quantity")));
	}

	private void reset(String symbol, String price) {
		Quote quote = quotes.findById(symbol).orElseThrow();
		quote.setPrice(new BigDecimal(price));
		quote.setAsOf(Instant.now());
		quotes.saveAndFlush(quote);
	}

	private String register(String label) throws Exception {
		String body = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(email(label), "paper-trade")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		int tokenStart = body.indexOf("\"token\":\"") + 9;
		return body.substring(tokenStart, body.indexOf('"', tokenStart));
	}

	private static String email(String label) {
		return label + "-" + java.util.UUID.randomUUID() + "@nexis.test";
	}

	private static String credentials(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	private static String order(String symbol, String side, int quantity) {
		return "{\"symbol\":\"" + symbol + "\",\"side\":\"" + side + "\",\"quantity\":" + quantity + "}";
	}
}
