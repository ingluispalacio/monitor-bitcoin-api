package com.micro.service.crypto_monitor.scheduler;

import com.micro.service.crypto_monitor.callservice.CallServicePriceHttp;
import com.micro.service.crypto_monitor.enums.EventType;
import com.micro.service.crypto_monitor.websocket.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceScheduler {

    private final CallServicePriceHttp callServicePriceHttp;
    private final EventBus eventBus;

    @Scheduled(fixedRate = 5000)
    public void broadcastBitcoinPrice() {
        log.info("📡 Iniciando consulta a CoinGecko...");

        callServicePriceHttp.getBitcoinPrice()
                .doOnError(e -> log.error("❌ Error en la llamada HTTP externa: {}", e.getMessage()))
                .subscribe(price -> {
                    if (price == null) {
                        log.warn("⚠️ CoinGecko respondió, pero el precio es NULL");
                        return;
                    }

                    log.info("💰 PRECIO RECIBIDO: {}", price);

                    Map<String, Object> data = new HashMap<>();
                    data.put("symbol", "BTC");
                    data.put("name", "Bitcoin");
                    data.put("price", price);
                    data.put("timestamp", System.currentTimeMillis());

                    log.info("📤 Publicando en EventBus: {}", data);
                    eventBus.publish(EventType.PRICE_UPDATE, data);
                });
    }
}