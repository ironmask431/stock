package org.example.stock.facade;

import org.example.stock.domain.Stock;
import org.example.stock.repository.StockRepository;
import org.example.stock.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LettuceLockFacadeTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private LettuceLockFacade lettuceLockFacade;

    @BeforeEach
    void beforeEach() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    void afterEach() {
        stockRepository.deleteAll();
    }

    @Test
    void 동시에_100건요청_redis_setnx_사용() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lettuceLockFacade.decrease(1L, 1L);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100개 - (1 * 100) = 0개 예상
        assertEquals(0L, stock.getQuantity());
    }

}