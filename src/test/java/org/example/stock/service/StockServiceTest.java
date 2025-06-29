package org.example.stock.service;

import jakarta.transaction.Transactional;
import org.example.stock.domain.Stock;
import org.example.stock.facade.OptimisticLockFacade;
import org.example.stock.repository.StockRepository;
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
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OptimisticLockFacade optimisticLockFacade;

    @BeforeEach
    void beforeEach() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    void afterEach() {
        stockRepository.deleteAll();
    }

    @Test
    void 재고_감소() {
        stockService.decrease(1L, 10L);
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(90L, stock.getQuantity());
    }

    @Test
    void 동시에_100건요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                    System.out.println("leesh stockService.decrease");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                    System.out.println("leesh latch.countDown");
                }
            });
        }

        latch.await();
        System.out.println("leesh await end");
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100개 - (1 * 100) = 0개 예상
        assertEquals(0L, stock.getQuantity());
    }

    @Test
    void 동시에_100건요청_비관락사용() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreasePessimistic(1L, 1L);
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

    @Test
    void 동시에_100건요청_낙관락사용() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockFacade.decrease(1L, 1L);
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