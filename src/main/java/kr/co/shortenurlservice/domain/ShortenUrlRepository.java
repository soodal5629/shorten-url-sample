package kr.co.shortenurlservice.domain;

import java.util.List;

public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);

    void asyncSaveShortenUrl(ShortenUrl shortenUrl);

    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);

    void increaseRedirectCount(ShortenUrl shortenUrl);
}
