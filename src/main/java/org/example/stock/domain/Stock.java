package org.example.stock.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Stock {

    // 낙관적 락을 사용하기 위한 version 추가
    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    protected Stock() {
    }

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalArgumentException("quantity 가 부족합니다.");
        }
        this.quantity -= quantity;
    }


}
