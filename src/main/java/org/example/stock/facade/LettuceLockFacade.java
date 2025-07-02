package org.example.stock.facade;

import lombok.RequiredArgsConstructor;
import org.example.stock.repository.RedisLockRepository;
import org.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockFacade {

    private final RedisLockRepository redisLockRepository;

    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        // redis에서 상품id 의 key 설정 실패하면 (이미 다른 스레드에서 선점 상태면) 100ms 이후 재시도
        // 100ms의 텀을 두는 이유는 너무 짧으면 redis에 과부하가 발생 할 우려가 있음.
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }
        // 재고감소 로직 실행 후 상품 id에 해당하는 redis key 설정 해제.
        try {
            stockService.decrease(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}

