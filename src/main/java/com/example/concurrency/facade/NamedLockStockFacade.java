package com.example.concurrency.facade;

import com.example.concurrency.repository.NamedLockRepository;
import com.example.concurrency.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {
	private final NamedLockRepository namedLockRepository;
	private final StockService stockService;

	@Transactional
	public void decrease(Long id, Long quantity){
		try{
			namedLockRepository.getLock(id.toString());
			stockService.decrease(id, quantity);
		}finally {
			namedLockRepository.releaseLock(id.toString());
		}
	}

}
