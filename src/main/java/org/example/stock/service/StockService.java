package org.example.stock.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.stock.domain.Stock;
import org.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    //@Transactional
    public synchronized void decrease(Long id, Long quantity){
        // Stock 조회
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("id에 해당하는 stock가 없습니다."));
        // 재고 감소
        stock.decrease(quantity);
        stockRepository.save(stock);
    }

    @Transactional
    public void decreasePessimistic(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        if (stock == null) {
            throw new RuntimeException("id에 해당하는 stock가 없습니다.");
        }
        // 재고 감소
        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
