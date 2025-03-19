package kr.co.shortenurlservice.domain;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);

    void asyncSaveShortenUrl(ShortenUrl shortenUrl);

    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);

    void increaseRedirectCount(ShortenUrl shortenUrl);

    @Scheduled(fixedRate = 10000) // 10초
    void updateRedirectCounts();
}
