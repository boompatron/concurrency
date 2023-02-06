package com.example.concurrency;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import com.example.concurrency.domain.Stock;
import com.example.concurrency.facade.OptimisticLockStockFacade;
import com.example.concurrency.repository.StockRepository;
import com.example.concurrency.service.PessimisticLockStockService;
import com.example.concurrency.service.StockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyApplicationTests {

	@Autowired
	StockRepository repository;

	@Autowired
	StockService stockService;

	@Autowired
	PessimisticLockStockService pessimisticLockStockService;

	@Autowired
	OptimisticLockStockFacade optimisticLockStockFacade;

	@BeforeEach
	void beforeEach() {
		repository.deleteAll();
		Stock stock = Stock.builder().productId(1L).quantity(100L).build();
		repository.save(stock);
	}

	// @AfterEach
	// void afterEach(){
	// 	repository.deleteAll();
	// }

	@Test
	@DisplayName("재고 감소")
	void decreaseStock() {
		// Given

		// When
		stockService.decrease(1L, 1L);
		Stock stock = repository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(99L);
	}

	@Test
	@DisplayName("동시에 100개 감소")
	void decreaseConcurrency() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(threadNums);
		CountDownLatch latch = new CountDownLatch(threadNums);
		// 다른 쓰레드에서 실행 중인 작업이 종료 될 때 까지 기다리게 도움을 줌

		// When
		for (int i = 0; i < threadNums; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		Stock stock = repository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("비관적 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithPessimisticLock() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(threadNums);
		CountDownLatch latch = new CountDownLatch(threadNums);

		// When
		for (int i = 0; i < threadNums; i++) {
			executorService.submit(() -> {
				try {
					pessimisticLockStockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		Stock stock = repository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("낙관적 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithOptimisticLock() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(threadNums);
		CountDownLatch latch = new CountDownLatch(threadNums);

		// When
		for (int i = 0; i < threadNums; i++) {
			executorService.submit(() -> {
				try {
					optimisticLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					throw new RuntimeException("");
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		Stock stock = repository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

}
