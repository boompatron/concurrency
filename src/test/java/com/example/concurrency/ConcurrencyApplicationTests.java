package com.example.concurrency;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import com.example.concurrency.domain.Stock;
import com.example.concurrency.facade.LettuceLockStockFacade;
import com.example.concurrency.facade.NamedLockStockFacade;
import com.example.concurrency.facade.OptimisticLockStockFacade;
import com.example.concurrency.repository.StockRepository;
import com.example.concurrency.service.PessimisticLockStockService;
import com.example.concurrency.service.StockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyApplicationTests {

	@Autowired
	StockRepository stockRepository;

	@Autowired
	StockService stockService;

	@Autowired
	PessimisticLockStockService pessimisticLockStockService;

	@Autowired
	OptimisticLockStockFacade optimisticLockStockFacade;

	@Autowired
	NamedLockStockFacade namedLockStockFacade;

	@Autowired
	LettuceLockStockFacade lettuceLockStockFacade;

	@BeforeEach
	void beforeEach() {
		Stock stock = Stock.builder().productId(1L).quantity(100L).build();
		stockRepository.saveAndFlush(stock);
	}

	@AfterEach
	void afterEach() {
		stockRepository.deleteAll();
	}

	@Test
	@DisplayName("재고 감소")
	void decreaseStock() {
		// Given

		// When
		stockService.decrease(1L, 1L);
		Stock stock = stockRepository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(99L);
	}

	@Test
	@DisplayName("동시에 100개 감소")
	void decreaseConcurrency() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
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
		Stock stock = stockRepository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("비관적 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithPessimisticLock() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
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
		Stock stock = stockRepository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("낙관적 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithOptimisticLock() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
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
		Stock stock = stockRepository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("Named 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithNamedcLock() throws InterruptedException {
		// Given
		int threadNums = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadNums);

		// When
		for (int i = 0; i < threadNums; i++) {
			executorService.submit(() -> {
				try {
					namedLockStockFacade.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		Stock stock = stockRepository.findById(1L).orElseThrow(EntityNotFoundException::new);

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

	@Test
	@DisplayName("Redis Lettuce 락을 사용하면서 동시에 100개 감소")
	void decreaseConcurrencyWithRedisLettuceLock() throws InterruptedException {
		// Given
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// When
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					lettuceLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();

		// Then
		assertThat(stock.getQuantity()).isEqualTo(0L);
	}

}
