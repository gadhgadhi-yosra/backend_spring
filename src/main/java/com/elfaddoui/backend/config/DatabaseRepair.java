package com.elfaddoui.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Pragmatic one-off schema repair for legacy databases.
 *
 * We have seen production/dev Postgres schemas where "users.full_name" and/or "users.email"
 * were created as BYTEA instead of a text type. That breaks case-insensitive search queries
 * that rely on LOWER(...).
 *
 * Hibernate ddl-auto=update will not always change existing column types, so we repair them
 * on startup (PostgreSQL only).
 */
@Component
public class DatabaseRepair implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRepair.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseRepair(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!isPostgres()) {
            return;
        }

        try {
            var dbInfo = jdbcTemplate.queryForMap("""
                    select current_database() as db,
                           current_schema() as schema,
                           current_user as db_user,
                           inet_server_addr() as server_addr,
                           inet_server_port() as server_port
                    """);
            log.info("DB connection: db={}, schema={}, user={}, addr={}, port={}",
                    dbInfo.get("db"), dbInfo.get("schema"), dbInfo.get("db_user"), dbInfo.get("server_addr"), dbInfo.get("server_port"));
        } catch (Exception e) {
            log.warn("Could not query DB connection info (skipping).", e);
        }

        logUsersColumnTypes();

        // Only convert if the current column type is BYTEA. Conversion assumes UTF-8 content.
        // If the column is already a text type, the block is a no-op.
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                  IF EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = 'users'
                      AND column_name = 'full_name'
                      AND data_type = 'bytea'
                  ) THEN
                    ALTER TABLE users
                      ALTER COLUMN full_name TYPE VARCHAR(255)
                      USING convert_from(full_name, 'UTF8');
                  END IF;

                  IF EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = 'users'
                      AND column_name = 'email'
                      AND data_type = 'bytea'
                  ) THEN
                    ALTER TABLE users
                      ALTER COLUMN email TYPE VARCHAR(255)
                      USING convert_from(email, 'UTF8');
                  END IF;
                END
                $$;
                """);
    }

    private void logUsersColumnTypes() {
        try {
            var rows = jdbcTemplate.queryForList("""
                    select column_name, data_type, udt_name
                    from information_schema.columns
                    where table_schema = 'public'
                      and table_name = 'users'
                      and column_name in ('full_name', 'email')
                    order by column_name
                    """);
            if (rows.isEmpty()) {
                log.info("users table column types: (no rows found in information_schema for public.users)");
                return;
            }
            for (var row : rows) {
                log.info("users.{} type: {} ({})", row.get("column_name"), row.get("data_type"), row.get("udt_name"));
            }
        } catch (Exception e) {
            log.warn("Could not query users column types (skipping).", e);
        }
    }

    private boolean isPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgresql");
        } catch (Exception e) {
            // If we can't detect, don't risk running dialect-specific SQL.
            log.warn("Skipping DatabaseRepair because DB product name could not be detected.", e);
            return false;
        }
    }
}
