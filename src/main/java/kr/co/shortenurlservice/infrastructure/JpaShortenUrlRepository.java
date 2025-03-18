package kr.co.shortenurlservice.infrastructure;

import kr.co.shortenurlservice.domain.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaShortenUrlRepository extends JpaRepository<ShortenUrl, Long> {
    ShortenUrl findByShortenUrlKey(String shortenUrlKey);

    @Modifying
    @Transactional
    @Query("UPDATE ShortenUrl s SET s.redirectCount = s.redirectCount + 1 WHERE s.shortenUrlKey = :shortenUrlKey")
    int incrementRedirectCount(String shortenUrlKey);
}