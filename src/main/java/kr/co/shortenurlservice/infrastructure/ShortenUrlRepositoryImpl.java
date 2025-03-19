package kr.co.shortenurlservice.infrastructure;

import kr.co.shortenurlservice.domain.ShortenUrl;
import kr.co.shortenurlservice.domain.ShortenUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class ShortenUrlRepositoryImpl implements ShortenUrlRepository {

    private final JpaShortenUrlRepository jpaShortenUrlRepository;
    private final ConcurrentHashMap<String, ShortenUrl> cache;
    private final ConcurrentHashMap<String, AtomicInteger> redirectCountMap;

    private final RedisTemplate<String, ShortenUrl> redisTemplate;
    private static final String CACHE_PREFIX = "shortenUrl::";

    @Autowired
    public ShortenUrlRepositoryImpl(JpaShortenUrlRepository jpaShortenUrlRepository, RedisTemplate<String, ShortenUrl> redisTemplate) {
        this.jpaShortenUrlRepository = jpaShortenUrlRepository;
        this.redirectCountMap = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        jpaShortenUrlRepository.save(shortenUrl);
        cache.put(shortenUrl.getShortenUrlKey(), shortenUrl);
        //redisTemplate.opsForValue().set(CACHE_PREFIX + shortenUrl.getShortenUrlKey(), shortenUrl);
    }

    @Async
    @Override
    public void asyncSaveShortenUrl(ShortenUrl shortenUrl) {
        jpaShortenUrlRepository.save(shortenUrl);
        cache.put(shortenUrl.getShortenUrlKey(), shortenUrl);
        //redisTemplate.opsForValue().set(CACHE_PREFIX + shortenUrl.getShortenUrlKey(), shortenUrl);
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
//        return jpaShortenUrlRepository.findByShortenUrlKey(shortenUrlKey);
        // 먼저 캐시에서 조회하도록 성능 개선
        ShortenUrl shortenUrl = cache.get(shortenUrlKey);
        //ShortenUrl shortenUrl = redisTemplate.opsForValue().get(CACHE_PREFIX + shortenUrlKey);
        if(shortenUrl == null) {
            // 캐시에 없으면 db에서 조회
            shortenUrl = jpaShortenUrlRepository.findByShortenUrlKey(shortenUrlKey);
            if(shortenUrl != null) {
                // db에 있으면 캐시에 저장
                cache.put(shortenUrlKey, shortenUrl);
                //redisTemplate.opsForValue().set(CACHE_PREFIX + shortenUrl.getShortenUrlKey(), shortenUrl);
            }
        }
        return shortenUrl;
    }

    @Override
    public void increaseRedirectCount(ShortenUrl shortenUrl) {
        //jpaShortenUrlRepository.incrementRedirectCount(shortenUrl.getShortenUrlKey());
        // redirect count 10초마다 한번에 처리
        String key = shortenUrl.getShortenUrlKey();
        // 해당 key에 대한 값이 없으면 0으로 세팅하고 존재한다면 1 증가하여 세팅
        redirectCountMap.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();

    }

    @Override
    @Scheduled(fixedRate = 10000)
    public void updateRedirectCounts() {
        redirectCountMap.forEach((key, count) -> {
            // count의 값을 increment에 할당하고 0으로 다시 세팅
            int increment = count.getAndSet(0);
            if(increment > 0) {
                jpaShortenUrlRepository.incrementRedirectCount(key, increment);
            }
        });
    }
}
