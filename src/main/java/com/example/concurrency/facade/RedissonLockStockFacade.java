package com.example.concurrency.facade;

import com.example.concurrency.service.StockService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedissonLockStockFacade {
	private final RedissonClient redissonClient;

	private final StockService stockService;

	public void decrease(Long key, Long quantity){
		RLock rLock = redissonClient.getLock(key.toString());

			try {
				boolean flag = rLock.tryLock(5, 1, TimeUnit.SECONDS);

				if (!flag){
					System.out.println("lock  failure");
					return;
				}

				stockService.decrease(key, quantity) ;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}finally {
				rLock.unlock();
			}
	}
}
