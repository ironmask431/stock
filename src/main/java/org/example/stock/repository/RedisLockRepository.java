package org.example.stock.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, String> redisTemplate;

    // 상품 id를 key로 받아서 setnx 설정.
    // 3초의 만료시간을 두는이유 = 분산 락을 Redis로 구현할 때, 락을 획득한 프로세스가 예상치 못하게 종료되거나
    // 장애가 발생하면 unlock()을 호출하지 못하고 락이 영구히 유지될 위험이 있으므로. 해당 현상 빙지용
    public Boolean lock(Long key) {
        return redisTemplate.opsForValue().setIfAbsent(key.toString(), "lock", Duration.ofSeconds(3));
    }

    // key 해제
    public Boolean unlock(Long key) {

        return redisTemplate.delete(key.toString());
    }

}
