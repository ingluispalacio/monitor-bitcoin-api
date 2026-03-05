package com.micro.service.crypto_monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.micro.service.crypto_monitor.callservice.CoinGeckoHttpClient;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class ApiConfig {

    @Value("${services.coingecko.base-url}")
    private String COINGECKO_BASE_URL;
   
    @Bean
    public Retrofit coinGeckoRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(COINGECKO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Bean
    public CoinGeckoHttpClient coinGeckoHttpClient(Retrofit coinGeckoRetrofit) {
        return coinGeckoRetrofit.create(CoinGeckoHttpClient.class);
    }
}