package com.micro.service.crypto_monitor.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApiConfig {

    public <T> T getApi(Class<T> apiClass, String baseUrl) {
        return getRetrofit(baseUrl).create(apiClass);
    }

    private Retrofit getRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}