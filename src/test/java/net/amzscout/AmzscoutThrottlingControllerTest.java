package net.amzscout;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AmzscoutThrottlingControllerTest {
    @Autowired
    private WebTestClient webClient;

    @Test
    public void sendRequests() throws InterruptedException {

        checkLimitRequests();

        List<EntityExchangeResult<String>> request = IntStream.range(0, 1000)
                .parallel()
                .mapToObj(i -> {
                    var ipAddress = getRandomIpAddress();
                    var result = webClient.get().uri("/amzscout")
                            .header("X-Forwarded-For", ipAddress)
                            .accept(MediaType.ALL)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody(String.class)
                            .returnResult();
                    System.out.printf("Response status: %s | IP Address: %s\n", result.getStatus().value(), ipAddress);

                    return result;
                })
                .collect(Collectors.toList());

        Thread.sleep(60000);
        checkLimitRequests();
    }

    private void checkLimitRequests() {
        var ipAddress = "171.162.32.37";

        for (int i = 0; i < 5; i++) {
            EntityExchangeResult<String> result = webClient.get().uri("/amzscout")
                    .header("X-Forwarded-For", ipAddress)
                    .accept(MediaType.ALL)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .returnResult();

            System.out.printf("Response status: %s | IP Address: %s\n", result.getStatus().value(), ipAddress);
        }

        EntityExchangeResult<String> result = webClient.get().uri("/amzscout")
                .header("X-Forwarded-For", ipAddress)
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .returnResult();

        System.out.printf("Response status: %s | IP Address: %s\n", result.getStatus().value(), ipAddress);
    }

    private String getRandomIpAddress() {
        return ThreadLocalRandom.current()
                .ints(4, 1, 255)
                .mapToObj(Integer::toString)
                .reduce((s1, s2) -> s1 + "." + s2)
                .orElse("127.0.0.1");
    }
}