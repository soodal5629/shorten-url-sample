package kr.co.shortenurlservice.domain;

import java.util.concurrent.atomic.AtomicLong;

public class SnowflakeKeyGenerator {
    private static final String BASE56_CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpgrstuvwxyz";
    private static final int BASE56 = BASE56_CHARACTERS.length();
    private static final long EPOCH = 1609459200000L; // Custom epoch
    private static final int SERVER_ID_BITS = 5;
    private static final int SEQUENCE_BITS = 12;
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;
    private static final int SERVER_ID = 1; // 편의상 하드코딩, 원래는 실제 서버 ID 주입시켜야 함
    private static AtomicLong sequence = new AtomicLong(0);
    private static volatile long lastTimestamp = -1L;

    public static String generateSnowflakeKey() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            // 뭔가 시간이 잘못된 것임
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 동일한 시간이 사용되고 있다면 중복되지 않도록 시퀀스 사용
        if (timestamp == lastTimestamp) {
            long currentSequence = sequence.incrementAndGet() & MAX_SEQUENCE;
            if(currentSequence == 0) {
                // Sequence overflow, wait till new millisecond
                timestamp = waitUntilNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }

        lastTimestamp = timestamp;
        long id = ((timestamp - EPOCH) << (SERVER_ID_BITS + SEQUENCE_BITS))
                | (SERVER_ID << SEQUENCE_BITS)
                | sequence.get();

        return encodeBase56(id);
    }

    private static long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static String encodeBase56(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            int index = (int) (id % BASE56);
            sb.append(BASE56_CHARACTERS.charAt(index));
            id /= BASE56;
        }
        while(sb.length() < 8) {
            sb.append(BASE56_CHARACTERS.charAt(0));
        }
        return sb.reverse().toString();
    }
}
