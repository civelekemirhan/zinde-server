package com.wexec.zinde_server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Eski şema artıklarını temizler.
 * Hibernate ddl-auto=update silinmiş alanları otomatik düşürmediği için
 * bu sınıf startup'ta bir kez çalışır, yoksa sessizce geçer.
 */
@Slf4j
@Component
@Order(1) // DataInitializer'dan önce çalışsın
@RequiredArgsConstructor
public class SchemaCleanup implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        dropColumnIfExists("follow_requests", "status");
        dropColumnIfExists("follow_requests", "responded_at");
    }

    private void dropColumnIfExists(String table, String column) {
        try {
            jdbc.execute("ALTER TABLE " + table + " DROP COLUMN IF EXISTS " + column);
            log.info("SchemaCleanup: {}.{} düşürüldü (veya zaten yoktu).", table, column);
        } catch (Exception e) {
            log.warn("SchemaCleanup: {}.{} düşürülemedi: {}", table, column, e.getMessage());
        }
    }
}
