package org.example.stock.facade;

import lombok.RequiredArgsConstructor;
import org.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimisticLockFacade {
    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        //낙관락은 버전차이로 인한 exception 발생 시 재시도 해야하므로 while문
        while (true) {
            try {
                // 재고 감소
                stockService.decreaseOptimistic(id, quantity);
                break; // 정상 실행 시 종료
            } catch (Exception e) {
                System.out.println(e + "/" + e.getMessage());
                Thread.sleep(50); // 버전차이로 인한 예외 발생 시 50ms 후 재시도.
            }
        }
    }
}
