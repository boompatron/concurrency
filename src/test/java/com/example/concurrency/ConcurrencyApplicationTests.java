package com.example.concurrency;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import com.example.concurrency.domain.Stock;
import com.example.concurrency.repository.StockRepository;
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
	StockService service;

	@BeforeEach
	void beforeEach(){
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
	void decreaseStock(){
		// Given

		// When
		service.decrease(1L, 1L);
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
		for (int i = 0; i < threadNums; i++){
			executorService.submit(() -> {
				try {
					service.decrease(1L, 1L);
				}finally {
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
