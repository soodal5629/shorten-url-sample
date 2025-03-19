package kr.co.shortenurlservice.infrastructure;

import kr.co.shortenurlservice.domain.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JpaShortenUrlRepository extends JpaRepository<ShortenUrl, Long> {
    ShortenUrl findByShortenUrlKey(String shortenUrlKey);

    @Modifying
    @Transactional
    //@Query("UPDATE ShortenUrl s SET s.redirectCount = s.redirectCount + 1 WHERE s.shortenUrlKey = :shortenUrlKey")
    @Query("UPDATE ShortenUrl s SET s.redirectCount = :increment WHERE s.shortenUrlKey = :shortenUrlKey")
    int incrementRedirectCount(@Param("shortenUrlKey") String shortenUrlKey, @Param("increment") int increment);
}