package com.example.concurrency.service;

import com.example.concurrency.domain.Stock;
import com.example.concurrency.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockStockService {
	private final StockRepository repository;

	@Transactional
	public void decrease(Long id, Long quantity){
		Stock stock = repository.findByIdWithPessimisticLock(id);

		stock.decrease(quantity);

		repository.saveAndFlush(stock);
	}
}
