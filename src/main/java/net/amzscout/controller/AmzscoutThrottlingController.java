package net.amzscout.controller;

import lombok.RequiredArgsConstructor;
import net.amzscout.service.AmzscoutThrottlingService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class AmzscoutThrottlingController {

    public static final String KEY = "X-Forwarded-For";

    private final AmzscoutThrottlingService amzscoutThrottlingService;

    @GetMapping("/amzscout")
    public Mono<Void> amzscout(ServerHttpRequest request) {
        return amzscoutThrottlingService.checkLimitRequest(
                        Objects.nonNull(request.getHeaders().get(KEY))
                                ? String.valueOf(request.getHeaders().get(KEY))
                                : "127.0.0.1",
                        request.getId()
                );
    }
}
