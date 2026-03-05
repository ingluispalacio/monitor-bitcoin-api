package com.micro.service.crypto_monitor.callservice;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.micro.service.crypto_monitor.dto.CoinGeckoPriceResponseDTO;

public interface CoinGeckoHttpClient {

    @GET("simple/price")
    Call<CoinGeckoPriceResponseDTO> getBitcoinPrice(
        @Query("ids") String cryptoId,
        @Query("vs_currencies") String currency,
        @Query("include_market_cap") boolean includeMarketCap,
        @Query("include_24hr_vol") boolean include24hrVol
    );
}
