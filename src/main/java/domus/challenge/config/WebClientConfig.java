package domus.challenge.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(MovieApiConfig movieApiConfig) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, movieApiConfig.getConnectTimeout())
                .responseTimeout(movieApiConfig.getResponseTimeout())
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(
                                        (int) movieApiConfig.getReadTimeout().toSeconds()))
                                .addHandlerLast(new WriteTimeoutHandler(
                                        (int) movieApiConfig.getWriteTimeout().toSeconds()))
                );

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg ->
                        cfg.defaultCodecs().maxInMemorySize(
                                (int) movieApiConfig.getMaxInMemorySize().toBytes()
                        )
                ).build();

        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);

        if (movieApiConfig.isLoggingEnabled()) {
            builder.filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                log.debug("WebClient Request: {} {}", request.method(), request.url());
                return Mono.just(request);
            }).andThen(ExchangeFilterFunction.ofResponseProcessor(response -> {
                log.debug("WebClient Response status: {}", response.statusCode());
                return Mono.just(response);
            })));
        }

        return builder;
    }

    @Bean
    public WebClient movieWebClient(WebClient.Builder builder, MovieApiConfig cfg) {
        return builder
                .baseUrl(cfg.getBaseUrl())
                .build();
    }

}


