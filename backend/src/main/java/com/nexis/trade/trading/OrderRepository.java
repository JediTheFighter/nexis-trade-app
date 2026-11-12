package com.nexis.trade.trading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<TradeOrder, UUID> {

	List<TradeOrder> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
