package com.flypass.financial.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initSequences() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS savings_account_seq START 1 INCREMENT 1");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS checking_account_seq START 1 INCREMENT 1");
        log.info("Secuencias de números de cuenta verificadas/creadas correctamente");
    }
}
