package kr.co.shortenurlservice.application;

import kr.co.shortenurlservice.domain.*;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateRequestDto;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateResponseDto;
import kr.co.shortenurlservice.presentation.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    @Transactional(readOnly = false)
    public ShortenUrlCreateResponseDto generateShortenUrl(ShortenUrlCreateRequestDto shortenUrlCreateRequestDto) {
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        String shortenUrlKey = getUniqueShortenUrlKey();

        ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);
        // 비동기로 db 저장
        shortenUrlRepository.asyncSaveShortenUrl(shortenUrl);

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    @Transactional(readOnly = false)
    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(null == shortenUrl)
            throw new NotFoundShortenUrlException();

        shortenUrl.increaseRedirectCount();
        //shortenUrlRepository.saveShortenUrl(shortenUrl);
        shortenUrlRepository.increaseRedirectCount(shortenUrl);

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(null == shortenUrl)
            throw new NotFoundShortenUrlException();

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    private String getUniqueShortenUrlKey() {
//        final int MAX_RETRY_COUNT = 5;
//        int count = 0;
//
//        while(count++ < MAX_RETRY_COUNT) {
//            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
//            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);
//
//            if(null == shortenUrl)
//                return shortenUrlKey;
//        }
//
//        throw new LackOfShortenUrlKeyException();

        // 애초에 중복이 발생하지 않도록 키 생성 - 스노우 플레이크 적용하여 성능 향상
        return SnowflakeKeyGenerator.generateSnowflakeKey();

    }

}
