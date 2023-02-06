package com.example.concurrency.service;

import com.example.concurrency.domain.Stock;
import com.example.concurrency.repository.StockRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {
	private final StockRepository stockRepository;

	public synchronized void decrease(Long id, Long quantity){
		  Stock stock = stockRepository.findById(id).orElseThrow(EntityNotFoundException::new);

			stock.decrease(quantity);

			stockRepository.saveAndFlush(stock);
	}
}
