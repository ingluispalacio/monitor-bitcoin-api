package com.micro.service.crypto_monitor.scheduler;

import com.micro.service.crypto_monitor.business.PriceService;
import com.micro.service.crypto_monitor.enums.EventType;
import com.micro.service.crypto_monitor.websocket.EventBus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PriceScheduler {

    private final PriceService priceService;
    private final EventBus eventBus;

    @Scheduled(fixedRate = 5000)
    public void broadcastBitcoinPrice() {

        log.info("📡 Iniciando consulta a CoinGecko...");

        priceService.getBitcoinPrice()
                .doOnError(e -> log.error("Error en llamada externa: {}", e.getMessage()))
                .subscribe(price -> {

                    if (price == null) {
                        log.warn("Precio NULL recibido");
                        return;
                    }

                    log.info("Precio recibido: {}", price);

                    Map<String, Object> data = new HashMap<>();
                    data.put("symbol", "BTC");
                    data.put("name", "Bitcoin");
                    data.put("price", price);
                    data.put("timestamp", System.currentTimeMillis());

                    log.info("Publicando evento en EventBus");

                    eventBus.publish(EventType.PRICE_UPDATE, data);
                });
    }
}