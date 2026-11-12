package com.nexis.trade.auth;

import com.nexis.trade.api.ApiException;
import com.nexis.trade.api.AuthRequest;
import com.nexis.trade.api.AuthResponse;
import com.nexis.trade.config.AppProperties;
import com.nexis.trade.trading.Account;
import com.nexis.trade.trading.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

	private final UserRepository users;
	private final AccountRepository accounts;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AppProperties properties;

	public AuthService(
			UserRepository users,
			AccountRepository accounts,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AppProperties properties) {
		this.users = users;
		this.accounts = accounts;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.properties = properties;
	}

	@Transactional
	public AuthResponse register(AuthRequest request) {
		String email = request.email().trim().toLowerCase();
		if (users.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "An account with that email already exists.");
		}
		User user = new User(UUID.randomUUID(), email, passwordEncoder.encode(request.password()), Instant.now());
		users.save(user);
		accounts.save(new Account(UUID.randomUUID(), user.getId(), properties.getTrading().getStartingCash()));
		return new AuthResponse(jwtService.issue(user.getId(), user.getEmail()), user.getEmail());
	}

	@Transactional(readOnly = true)
	public AuthResponse login(AuthRequest request) {
		String email = request.email().trim().toLowerCase();
		User user = users.findByEmail(email)
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is wrong."));
		return new AuthResponse(jwtService.issue(user.getId(), user.getEmail()), user.getEmail());
	}
}
