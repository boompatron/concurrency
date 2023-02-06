package com.example.concurrency.service;

import com.example.concurrency.domain.Stock;
import com.example.concurrency.repository.StockRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
	private final StockRepository stockRepository;

	@Transactional
	public void decrease(Long id, Long quantity){
		  Stock stock = stockRepository.findById(id).orElseThrow(EntityNotFoundException::new);

			stock.decrease(quantity);

			stockRepository.saveAndFlush(stock);
	}
}
