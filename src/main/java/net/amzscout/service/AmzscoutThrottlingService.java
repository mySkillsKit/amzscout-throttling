package net.amzscout.service;

import lombok.RequiredArgsConstructor;
import net.amzscout.exception.RequestLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@RequiredArgsConstructor
public class AmzscoutThrottlingService {

    @Value("${rate.limiter.max.requests}")
    private int maxRequests;
    @Value("${rate.limiter.interval.minutes}")
    private int intervalMinutes;

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

    public Mono<Void> checkLimitRequest(String ip, String id) {

        Deque<Instant> ipRequests = requests.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        var nowTime = Instant.now();

        while (!ipRequests.isEmpty() && nowTime.isAfter(ipRequests.getFirst().plus(Duration.ofMinutes(intervalMinutes)))) {
            ipRequests.removeFirst();
        }

        if (ipRequests.size() >= maxRequests) {
            throw new RequestLimitException(
                    String.format(
                            "Ограничено количество запросов с IP адреса:%s" +
                                    " на этот метод в размере %d штук в %d минут",
                            ip,
                            maxRequests,
                            intervalMinutes
                    ),
                    id
            );
        }
        ipRequests.addLast(nowTime);

        return Mono.empty();
    }
}