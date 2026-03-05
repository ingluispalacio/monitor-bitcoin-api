package com.micro.service.crypto_monitor.dto;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class CoinGeckoPriceResponseDTO {

    @SerializedName("bitcoin")
    private BitcoinPrice bitcoin;
    
    @Data
    public static class BitcoinPrice {
        @SerializedName("usd")
        private BigDecimal usd;
        
        @SerializedName("usd_market_cap")
        private BigDecimal usdMarketCap;
        
        @SerializedName("usd_24h_vol")
        private BigDecimal usd24hVol;
    }
}
