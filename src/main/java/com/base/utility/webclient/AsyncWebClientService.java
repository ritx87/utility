package com.base.utility.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncWebClientService {
    private final WebClient webClient;

    public <T> CompletableFuture<T> getAsync(String uri, Class<T> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }

    public <T> CompletableFuture<List<T>> getAllAsync(List<String> uris, Class<T> responseType) {
        return Flux.fromIterable(uris)
                .flatMap(uri ->
                        webClient.get()
                                .uri(uri)
                                .retrieve()
                                .bodyToMono(responseType)
                                .onErrorReturn(null) // Handle individual failures
                )
                .collectList()
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }

    public <T, R> CompletableFuture<R> postAsync(String uri, T requestBody, Class<R> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture();
    }
}
